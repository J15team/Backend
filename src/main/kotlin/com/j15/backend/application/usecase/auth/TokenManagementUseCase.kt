package com.j15.backend.application.usecase.auth

import com.j15.backend.domain.model.auth.AuthTokens
import com.j15.backend.domain.model.auth.RefreshToken
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.JwtTokenService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** Refresh Tokenを使用したAccess Token更新 */
@Service
@Transactional(readOnly = true)
class TokenManagementUseCase(
        private val userRepository: UserRepository,
        private val jwtTokenService: JwtTokenService
) {

    fun refreshAccessToken(refreshTokenString: String): AuthTokens {
        if (!jwtTokenService.validateToken(refreshTokenString)) {
            throw IllegalArgumentException("Refresh Tokenが無効です")
        }

        val userId = jwtTokenService.getUserIdFromToken(refreshTokenString)
        val role = jwtTokenService.getRoleFromToken(refreshTokenString)

        val user = userRepository.findById(userId) ?: throw IllegalArgumentException("ユーザーが見つかりません")

        val newAccessToken = jwtTokenService.generateAccessToken(user.userId, user.role)
        val refreshToken = RefreshToken(refreshTokenString)

        return AuthTokens(newAccessToken, refreshToken)
    }
}
