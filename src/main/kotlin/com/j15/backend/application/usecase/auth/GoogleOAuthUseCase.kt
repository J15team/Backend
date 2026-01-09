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
 * フロントエンドから受け取った認証コードを使用して:
 * 1. Googleからユーザー情報を取得
 * 2. 既存ユーザーの場合はログイン、新規の場合は自動登録
 * 3. JWTトークンを発行
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
            val isNewUser: Boolean // 新規登録されたユーザーかどうか
    )

    /**
     * Googleの認証コードを使用してログイン/登録を行う
     *
     * @param authorizationCode Googleから受け取った認証コード
     * @return 認証結果（ユーザー情報とトークン）
     */
    fun authenticateWithGoogle(authorizationCode: String): OAuthResult {
        // Googleからユーザー情報を取得
        val googleUser = googleOAuthService.authenticateWithCode(authorizationCode)

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
        val tokens = generateTokens(user)
        return OAuthResult(user = user, tokens = tokens, isNewUser = false)
    }

    /** 新規ユーザーの登録処理 */
    private fun registerNewUser(googleUser: GoogleOAuthService.GoogleUserInfo): OAuthResult {
        // メールアドレスが既に登録されているかチェック
        val existingEmailUser = userRepository.findByEmail(Email(googleUser.email))
        if (existingEmailUser != null) {
            // 同じメールアドレスで既存アカウントがある場合
            // セキュリティ上、自動的にリンクせずエラーにする
            throw OAuthUserExistsException("このメールアドレスは既に別のアカウントで登録されています。" + "既存のアカウントでログインしてください。")
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
                        profileImageUrl = googleUser.picture
                )

        val savedUser = userRepository.save(newUser)
        val tokens = generateTokens(savedUser)

        return OAuthResult(user = savedUser, tokens = tokens, isNewUser = true)
    }

    /** JWTトークンを生成 */
    private fun generateTokens(user: User): AuthTokens {
        val accessToken = jwtTokenService.generateAccessToken(user.userId, user.role)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId, user.role)
        return AuthTokens(accessToken, refreshToken)
    }

    /** ユーザー名をサニタイズ（英数字とアンダースコアのみ、最大20文字） */
    private fun sanitizeUsername(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9_]"), "")
                .take(15) // 連番用に余裕を持たせる
                .ifEmpty { "user" }
    }

    /** 一意のユーザー名を生成（重複がある場合は連番を追加） */
    private fun generateUniqueUsername(baseUsername: String): String {
        var username = baseUsername
        var counter = 1

        while (userRepository.existsByUsername(Username(username))) {
            username = "${baseUsername}${counter}"
            counter++

            // 無限ループ防止
            if (counter > 1000) {
                username = "${baseUsername}${UUID.randomUUID().toString().take(8)}"
                break
            }
        }

        return username
    }
}

/** OAuth認証時に同じメールアドレスのユーザーが既に存在する場合の例外 */
class OAuthUserExistsException(message: String) : RuntimeException(message)
