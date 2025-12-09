package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest

/**
 * 管理者操作のユースケース
 */
@Service
@Transactional
class AdminUseCase(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.api-key:}")
    private val adminApiKey: String
) {

    /**
     * 管理者ユーザー作成の結果
     */
    data class AdminUserCreationResult(
        val user: User
    )

    /**
     * 管理者キーを検証して管理者アカウントを作成
     */
    fun createAdminUser(
        email: String,
        username: String,
        password: String,
        providedKey: String
    ): AdminUserCreationResult {
        // 管理者キーの検証（定数時間比較でタイミング攻撃を防止）
        validateAdminKey(providedKey)

        val emailVo = Email(email)
        val usernameVo = Username(username)

        // メールアドレスが既に使用されていないか確認
        if (userRepository.existsByEmail(emailVo)) {
            throw IllegalArgumentException("Email already registered")
        }

        // ユーザー名が既に使用されていないか確認
        if (userRepository.existsByUsername(usernameVo)) {
            throw IllegalArgumentException("Username already taken")
        }

        val hashedPassword = PasswordHash(passwordEncoder.encode(password))
        val adminUser = User(
            userId = UserId(),
            username = usernameVo,
            email = emailVo,
            passwordHash = hashedPassword,
            role = UserRole.ROLE_ADMIN
        )

        val savedUser = userRepository.save(adminUser)

        return AdminUserCreationResult(user = savedUser)
    }

    /**
     * 管理者キーを検証（定数時間比較）
     */
    private fun validateAdminKey(providedKey: String) {
        if (adminApiKey.isBlank()) {
            throw IllegalArgumentException("Admin API key is not configured")
        }

        if (!MessageDigest.isEqual(
            providedKey.toByteArray(Charsets.UTF_8),
            adminApiKey.toByteArray(Charsets.UTF_8)
        )) {
            throw IllegalArgumentException("Invalid admin API key")
        }
    }
}
