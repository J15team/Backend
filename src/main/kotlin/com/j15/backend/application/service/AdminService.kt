package com.j15.backend.application.service

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.MessageDigest

@Service
class AdminService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.api-key:}")
    private val adminApiKey: String
) {

    /**
     * 管理者キーを検証して管理者アカウントを作成
     */
    fun createAdminUser(
        request: AdminUserCreateRequest,
        providedKey: String
    ): AdminUserCreateResponse {
        // 管理者キーの検証（定数時間比較でタイミング攻撃を防止）
        if (adminApiKey.isBlank()) {
            throw IllegalArgumentException("Admin API key is not configured")
        }
        
        if (!MessageDigest.isEqual(
            providedKey.toByteArray(Charsets.UTF_8),
            adminApiKey.toByteArray(Charsets.UTF_8)
        )) {
            throw IllegalArgumentException("Invalid admin API key")
        }

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
        val adminUser = User(
            userId = UserId(),
            username = username,
            email = email,
            passwordHash = hashedPassword,
            role = UserRole.ROLE_ADMIN
        )

        val savedUser = userRepository.save(adminUser)

        return AdminUserCreateResponse(
            userId = savedUser.userId.value.toString(),
            email = savedUser.email.value,
            username = savedUser.username.value,
            role = savedUser.role.name
        )
    }
}
