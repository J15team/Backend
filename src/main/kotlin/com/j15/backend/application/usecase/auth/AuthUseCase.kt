package com.j15.backend.application.usecase.auth

import com.j15.backend.domain.model.auth.AuthTokens
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.domain.service.PasswordHashService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** ユーザー認証サービス（ログイン処理） */
@Service
@Transactional(readOnly = true)
class AuthUseCase(
        private val userRepository: UserRepository,
        private val passwordHashService: PasswordHashService,
        private val jwtTokenService: JwtTokenService
) {
    /** タイミング攻撃防止用ダミーハッシュ（処理時間を一定に保つ） */
    private val dummyHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.DOH7R4rE/pqKQf9p1mT4cXQF7lXjvC"

    data class AuthenticationResult(val user: User, val tokens: AuthTokens)

    fun authenticate(email: String, plainPassword: String): AuthenticationResult {
        val emailVo = Email(email)
        val user = userRepository.findByEmail(emailVo)

        val isPasswordValid =
                if (user != null) {
                    passwordHashService.verify(plainPassword, user.passwordHash.value)
                } else {
                    passwordHashService.verify(plainPassword, dummyHash)
                    false
                }

        if (user == null || !isPasswordValid) {
            throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません")
        }

        val accessToken = jwtTokenService.generateAccessToken(user.userId, user.role)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId, user.role)
        val tokens = AuthTokens(accessToken, refreshToken)

        return AuthenticationResult(user, tokens)
    }
}
