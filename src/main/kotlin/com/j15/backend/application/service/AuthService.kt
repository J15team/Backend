package com.j15.backend.application.service

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import com.j15.backend.presentation.dto.auth.SignInRequest
import com.j15.backend.presentation.dto.auth.SignInResponse
import com.j15.backend.presentation.dto.auth.SignUpRequest
import com.j15.backend.presentation.dto.auth.SignUpResponse
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
    fun signIn(request: SignInRequest): SignInResponse {
        val email = Email(request.email)
        val user = userRepository.findByEmail(email)
            ?: throw IllegalArgumentException("User not found with email: ${request.email}")

        if (!passwordEncoder.matches(request.password, user.passwordHash.value)) {
            throw IllegalArgumentException("Invalid credentials")
        }

        val accessToken = jwtTokenService.generateAccessToken(user.userId)
        val refreshToken = jwtTokenService.generateRefreshToken(user.userId)

        return SignInResponse(
            userId = user.userId.value.toString(),
            email = user.email.value,
            accessToken = accessToken.value,
            refreshToken = refreshToken.value
        )
    }

    /**
     * ユーザーサインアップ
     */
    fun signUp(request: SignUpRequest): SignUpResponse {
        val email = Email(request.email)
        val username = Username(request.username)

        // メールアドレスが既に使用されていないか確認
        if (userRepository.existsByEmail(email)) {
            throw IllegalArgumentException("Email already registered: ${request.email}")
        }

        // ユーザー名が既に使用されていないか確認
        if (userRepository.existsByUsername(username)) {
            throw IllegalArgumentException("Username already taken: ${request.username}")
        }

        val hashedPassword = PasswordHash(passwordEncoder.encode(request.password))
        val user = User(
            userId = UserId(),
            username = username,
            email = email,
            passwordHash = hashedPassword
        )

        val savedUser = userRepository.save(user)

        return SignUpResponse(
            userId = savedUser.userId.value.toString(),
            email = savedUser.email.value,
            username = savedUser.username.value
        )
    }

    /**
     * トークンリフレッシュ
     */
    fun refreshToken(refreshToken: String): String {
        if (!jwtTokenService.validateToken(refreshToken)) {
            throw IllegalArgumentException("Invalid refresh token")
        }

        val userId = jwtTokenService.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found")

        return jwtTokenService.generateAccessToken(user.userId).value
    }
}
