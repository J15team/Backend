package com.j15.backend.application.usecase.auth

import com.j15.backend.domain.model.auth.AuthTokens
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.OAuthProvider
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.user.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.infrastructure.service.GitHubOAuthService
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * GitHub OAuth認証ユースケース（アプリケーション層）
 *
 * Authorization Code方式をサポート
 */
@Service
@Transactional
class GitHubOAuthUseCase(
        private val gitHubOAuthService: GitHubOAuthService,
        private val userRepository: UserRepository,
        private val jwtTokenService: JwtTokenService
) {
    /** OAuth認証結果 */
    data class OAuthResult(
            val user: User,
            val tokens: AuthTokens,
            val isNewUser: Boolean,
            val isFirstLogin: Boolean
    )

    /**
     * GitHubの認証コードを使用してログイン/登録を行う
     *
     * @param code GitHubから受け取った認証コード
     * @return 認証結果（ユーザー情報とトークン）
     */
    fun authenticateWithGitHub(code: String): OAuthResult {
        val gitHubUser = gitHubOAuthService.authenticateWithCode(code)

        // 既存ユーザーを検索（GitHubのユーザーIDで）
        val existingUser = userRepository.findByOAuthProvider(OAuthProvider.GITHUB, gitHubUser.id)

        return if (existingUser != null) {
            loginExistingUser(existingUser)
        } else {
            registerNewUser(gitHubUser)
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
    private fun registerNewUser(gitHubUser: GitHubOAuthService.GitHubUserInfo): OAuthResult {
        // メールアドレスが既に登録されているかチェック
        val existingEmailUser = userRepository.findByEmail(Email(gitHubUser.email))
        if (existingEmailUser != null) {
            // 同じメールアドレスで既存アカウントがある場合は、そのユーザーとしてログイン
            return loginExistingUser(existingEmailUser)
        }

        // ユーザー名を生成
        val baseUsername = sanitizeUsername(gitHubUser.name)
        val username = generateUniqueUsername(baseUsername)

        val newUser =
                User(
                        userId = UserId(UUID.randomUUID()),
                        username = Username(username),
                        email = Email(gitHubUser.email),
                        passwordHash = null,
                        oauthProvider = OAuthProvider.GITHUB,
                        oauthProviderId = gitHubUser.id,
                        profileImageUrl = gitHubUser.avatarUrl,
                        loginCount = 1,
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

            if (counter > 1000) {
                val uuid = UUID.randomUUID().toString().take(8)
                username = "${baseUsername.take(50 - uuid.length - 1)}_$uuid"
                break
            }
        }

        return username
    }
}
