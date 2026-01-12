package com.j15.backend.application.usecase.auth

import com.j15.backend.domain.model.auth.AuthTokens
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.OAuthProvider
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.infrastructure.service.GoogleOAuthService
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Google OAuth認証ユースケース（アプリケーション層）
 *
 * 2つの認証方式をサポート:
 * 1. ID Token方式（推奨）: Google Identity Services (GIS) からのID Token
 * 2. Authorization Code方式（レガシー）: リダイレクトフローの認証コード
 */
@Service
@Transactional
class GoogleOAuthUseCase(
        private val googleOAuthService: GoogleOAuthService,
        private val userRepository: UserRepository,
        private val jwtTokenService: JwtTokenService
) {
    /** OAuth認証結果 */
    data class OAuthResult(
            val user: User,
            val tokens: AuthTokens,
            val isNewUser: Boolean, // 新規登録されたユーザーかどうか
            val isFirstLogin: Boolean // 初回ログインかどうか（チュートリアル表示用）
    )

    /**
     * ID Tokenを使用してログイン/登録を行う（推奨方式） Google Identity Services (GIS) のポップアップログインで取得したID Tokenを検証
     *
     * @param idToken Google Sign-Inから受け取ったID Token (credential)
     * @return 認証結果（ユーザー情報とトークン）
     */
    fun authenticateWithIdToken(idToken: String): OAuthResult {
        // GoogleでID Tokenを検証してユーザー情報を取得
        val googleUser = googleOAuthService.verifyIdToken(idToken)

        return processGoogleUser(googleUser)
    }

    /**
     * Googleの認証コードを使用してログイン/登録を行う（レガシー方式）
     *
     * @param authorizationCode Googleから受け取った認証コード
     * @return 認証結果（ユーザー情報とトークン）
     */
    fun authenticateWithGoogle(authorizationCode: String): OAuthResult {
        // Googleからユーザー情報を取得
        val googleUser = googleOAuthService.authenticateWithCode(authorizationCode)

        return processGoogleUser(googleUser)
    }

    /** Googleユーザー情報を処理してログイン/登録を行う（共通処理） */
    private fun processGoogleUser(googleUser: GoogleOAuthService.GoogleUserInfo): OAuthResult {
        // 既存ユーザーを検索（GoogleのユーザーIDで）
        val existingUser = userRepository.findByOAuthProvider(OAuthProvider.GOOGLE, googleUser.id)

        return if (existingUser != null) {
            // 既存ユーザーの場合: ログイン処理
            loginExistingUser(existingUser)
        } else {
            // 新規ユーザーの場合: 登録処理
            registerNewUser(googleUser)
        }
    }

    /** 既存ユーザーのログイン処理 */
    private fun loginExistingUser(user: User): OAuthResult {
        // 初回ログインかどうかを記録（カウント更新前に判定）
        val isFirstLogin = user.isFirstLogin

        // ログインカウントを更新
        val updatedUser =
                user.copy(loginCount = user.loginCount + 1, lastLoginAt = java.time.Instant.now())
        userRepository.save(updatedUser)

        val tokens = generateTokens(updatedUser)
        return OAuthResult(
                user = updatedUser,
                tokens = tokens,
                isNewUser = false,
                isFirstLogin = isFirstLogin
        )
    }

    /** 新規ユーザーの登録処理（または既存ユーザーへのログイン） */
    private fun registerNewUser(googleUser: GoogleOAuthService.GoogleUserInfo): OAuthResult {
        // メールアドレスが既に登録されているかチェック
        val existingEmailUser = userRepository.findByEmail(Email(googleUser.email))
        if (existingEmailUser != null) {
            // 同じメールアドレスで既存アカウントがある場合は、そのユーザーとしてログイン
            return loginExistingUser(existingEmailUser)
        }

        // ユーザー名を生成（Googleの名前をベースに、重複があれば連番を追加）
        val baseUsername = sanitizeUsername(googleUser.name)
        val username = generateUniqueUsername(baseUsername)

        // 新規ユーザーを作成
        val newUser =
                User(
                        userId = UserId(UUID.randomUUID()),
                        username = Username(username),
                        email = Email(googleUser.email),
                        passwordHash = null, // OAuthユーザーはパスワードなし
                        oauthProvider = OAuthProvider.GOOGLE,
                        oauthProviderId = googleUser.id,
                        profileImageUrl = googleUser.picture,
                        loginCount = 1, // 新規登録時は1回目のログイン
                        lastLoginAt = java.time.Instant.now()
                )

        val savedUser = userRepository.save(newUser)
        val tokens = generateTokens(savedUser)

        return OAuthResult(user = savedUser, tokens = tokens, isNewUser = true, isFirstLogin = true)
    }

    /** JWTトークンを生成 */
    private fun generateTokens(user: User): AuthTokens {
        val accessToken = jwtTokenService.generateAccessToken(user.userId, user.role)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId, user.role)
        return AuthTokens(accessToken, refreshToken)
    }

    /** ユーザー名をサニタイズ（最大50文字、空の場合はデフォルト名） */
    private fun sanitizeUsername(name: String): String {
        return name.trim().take(50).ifBlank { "user" }
    }

    /** 一意のユーザー名を生成（重複がある場合は連番を追加） */
    private fun generateUniqueUsername(baseUsername: String): String {
        var username = baseUsername.take(50)
        var counter = 1

        while (userRepository.existsByUsername(Username(username))) {
            val suffix = counter.toString()
            username = "${baseUsername.take(50 - suffix.length)}$suffix"
            counter++

            // 無限ループ防止
            if (counter > 1000) {
                val uuid = UUID.randomUUID().toString().take(8)
                username = "${baseUsername.take(50 - uuid.length - 1)}_$uuid"
                break
            }
        }

        return username
    }
}
