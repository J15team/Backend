package com.j15.backend.application.service

import com.j15.backend.application.command.RefreshTokenCommand
import com.j15.backend.application.command.SignInCommand
import com.j15.backend.application.command.SignUpCommand
import com.j15.backend.domain.exception.InvalidTokenException
import com.j15.backend.domain.exception.UserNotFoundException
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.domain.service.PasswordHashService
import com.j15.backend.domain.service.UserDuplicationCheckService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService,
    private val passwordHashService: PasswordHashService,
    private val duplicationCheckService: UserDuplicationCheckService
) {
    // タイミング攻撃を防ぐための有効なダミーBCryptハッシュ
    private val dummyHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.DOH7R4rE/pqKQf9p1mT4cXQF7lXjvC"

    /**
     * ユーザーサインイン
     */
    @Transactional(readOnly = true)
    fun signIn(command: SignInCommand): SignInResult {
        val email = Email(command.email)
        val user = userRepository.findByEmail(email)
        
        // タイミング攻撃を防ぐため、ユーザーが存在しない場合でもパスワード検証を実行
        val isPasswordValid = if (user != null) {
            passwordHashService.verify(command.password, user.passwordHash.value)
        } else {
            // ダミーのハッシュで検証を実行してタイミングを一定に保つ
            passwordHashService.verify(command.password, dummyHash)
            false
        }
        
        if (user == null || !isPasswordValid) {
            throw com.j15.backend.domain.exception.InvalidCredentialsException()
        }

        val accessToken = jwtTokenService.generateAccessToken(user.userId)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId)

        return SignInResult(
            userId = user.userId.value.toString(),
            email = user.email.value,
            accessToken = accessToken.value,
            refreshToken = refreshToken.value
        )
    }

    /**
     * ユーザーサインアップ
     */
    @Transactional
    fun signUp(command: SignUpCommand): SignUpResult {
        val email = Email(command.email)
        val username = Username(command.username)

        // 重複チェックサービスを使用
        duplicationCheckService.checkEmailAvailable(email)
        duplicationCheckService.checkUsernameAvailable(username)

        val hashedPassword = PasswordHash(passwordHashService.hash(command.password))
        val user = User(
            userId = UserId(),
            username = username,
            email = email,
            passwordHash = hashedPassword
        )

        val savedUser = userRepository.save(user)

        return SignUpResult(
            userId = savedUser.userId.value.toString(),
            email = savedUser.email.value,
            username = savedUser.username.value
        )
    }

    /**
     * トークンリフレッシュ
     */
    @Transactional(readOnly = true)
    fun refreshToken(command: RefreshTokenCommand): String {
        if (!jwtTokenService.validateToken(command.refreshToken)) {
            throw InvalidTokenException("リフレッシュトークンが不正です")
        }

        val userId = jwtTokenService.getUserIdFromToken(command.refreshToken)
        val user = userRepository.findById(userId)
            ?: throw UserNotFoundException()

        return jwtTokenService.generateAccessToken(user.userId).value
    }
    
    // 結果オブジェクト
    data class SignInResult(
        val userId: String,
        val email: String,
        val accessToken: String,
        val refreshToken: String
    )
    
    data class SignUpResult(
        val userId: String,
        val email: String,
        val username: String
    )
}
