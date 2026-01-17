package com.j15.backend.application.usecase.auth

import com.j15.backend.domain.model.auth.AuthTokens
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.repository.user.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.domain.service.PasswordHashService
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** ユーザー認証サービス（ログイン処理） */
@Service
@Transactional
class AuthUseCase(
        private val userRepository: UserRepository,
        private val passwordHashService: PasswordHashService,
        private val jwtTokenService: JwtTokenService
) {
    /** タイミング攻撃防止用ダミーハッシュ（処理時間を一定に保つ） */
    private val dummyHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.DOH7R4rE/pqKQf9p1mT4cXQF7lXjvC"

    data class AuthenticationResult(
            val user: User,
            val tokens: AuthTokens,
            val isFirstLogin: Boolean
    )

    fun authenticate(email: String, plainPassword: String): AuthenticationResult {
        val emailVo = Email(email)
        val user = userRepository.findByEmail(emailVo)

        // OAuthユーザーの場合はパスワードログイン不可
        if (user != null && user.passwordHash == null) {
            throw IllegalArgumentException("このアカウントはGoogleログインで作成されています。Googleでログインしてください。")
        }

        val isPasswordValid =
                if (user != null && user.passwordHash != null) {
                    passwordHashService.verify(plainPassword, user.passwordHash.value)
                } else {
                    passwordHashService.verify(plainPassword, dummyHash)
                    false
                }

        if (user == null || !isPasswordValid) {
            throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません")
        }

        // 初回ログインかどうかを記録（カウント更新前に判定）
        val isFirstLogin = user.isFirstLogin

        // ログインカウントを更新
        val updatedUser = user.copy(loginCount = user.loginCount + 1, lastLoginAt = Instant.now())
        userRepository.save(updatedUser)

        val accessToken = jwtTokenService.generateAccessToken(updatedUser.userId, updatedUser.role)
        val refreshToken =
                jwtTokenService.generateRefreshToken(updatedUser.userId, updatedUser.role)
        val tokens = AuthTokens(accessToken, refreshToken)

        return AuthenticationResult(updatedUser, tokens, isFirstLogin)
    }
}
