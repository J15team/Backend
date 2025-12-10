package com.j15.backend.application.service

import com.j15.backend.application.command.SignInCommand
import com.j15.backend.application.command.SignInResult
import com.j15.backend.application.command.SignUpCommand
import com.j15.backend.application.command.SignUpResult
import com.j15.backend.application.command.RefreshTokenCommand
import com.j15.backend.application.command.RefreshTokenResult
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val jwtTokenService: JwtTokenService,
    private val passwordEncoder: PasswordEncoder
) {

    /**
     * ユーザーサインイン
     */
    fun signIn(command: SignInCommand): SignInResult {
        val email = Email(command.email)
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found with email: ${command.email}")

        if (!passwordEncoder.matches(command.password, user.passwordHash.value)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val accessToken = jwtTokenService.generateAccessToken(user.userId, user.role)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId, user.role)

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
    fun signUp(command: SignUpCommand): SignUpResult {
        val email = Email(command.email)
        val username = Username(command.username)

        // メールアドレスが既に使用されていないか確認
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already registered: ${command.email}")
        }

        // ユーザー名が既に使用されていないか確認
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already taken: ${command.username}")
        }

        val hashedPassword = PasswordHash(passwordEncoder.encode(command.password))
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
    fun refreshToken(command: RefreshTokenCommand): RefreshTokenResult {
        if (!jwtTokenService.validateToken(command.refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtTokenService.getUserIdFromToken(command.refreshToken)
        val role = jwtTokenService.getRoleFromToken(command.refreshToken)
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        return RefreshTokenResult(
            accessToken = jwtTokenService.generateAccessToken(user.userId, role).value
        )
    }
}
