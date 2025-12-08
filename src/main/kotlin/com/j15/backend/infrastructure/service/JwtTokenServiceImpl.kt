package com.j15.backend.infrastructure.service

import com.j15.backend.domain.model.auth.AccessToken
import com.j15.backend.domain.model.auth.RefreshToken
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.service.JwtTokenService
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey

@Service
class JwtTokenServiceImpl(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expiration}") private val accessTokenExpiration: Long,
    @Value("\${jwt.refresh-token-expiration}") private val refreshTokenExpiration: Long
) : JwtTokenService {

    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray(Charsets.UTF_8))
    }

    override fun generateAccessToken(userId: UserId, role: UserRole): AccessToken {
        val token = createToken(userId, role, accessTokenExpiration)
        return AccessToken(token)
    }

    override fun generateRefreshToken(userId: UserId, role: UserRole): RefreshToken {
        val token = createToken(userId, role, refreshTokenExpiration)
        return RefreshToken(token)
    }

    override fun getUserIdFromToken(token: String): UserId {
        val claims = extractAllClaims(token)
        val userIdString = claims.subject
        return UserId(UUID.fromString(userIdString))
    }

    override fun getRoleFromToken(token: String): UserRole {
        val claims = extractAllClaims(token)
        val roleString = claims.get("role", String::class.java)
        return UserRole.fromString(roleString)
    }

    override fun validateToken(token: String): Boolean {
        return try {
            val claims = extractAllClaims(token)
            !isTokenExpired(claims)
        } catch (e: Exception) {
            false
        }
    }

    private fun createToken(userId: UserId, role: UserRole, expiration: Long): String {
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userId.value.toString())
            .claim("role", role.name)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    private fun extractAllClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun isTokenExpired(claims: Claims): Boolean {
        return claims.expiration.before(Date())
    }
}
