package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import jakarta.annotation.PostConstruct

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
    private val logger = LoggerFactory.getLogger(AdminUseCase::class.java)

    /**
     * 起動時にADMIN_API_KEYが適切に設定されているか検証
     */
    @PostConstruct
    fun validateConfig() {
        if (adminApiKey.isBlank()) {
            logger.error("ADMIN_API_KEY is not properly configured!")
            throw IllegalStateException("ADMIN_API_KEY must be set in production")
        }
        if (adminApiKey == "admin-secret-key-change-this-in-production") {
            logger.error("ADMIN_API_KEY is using default value!")
            throw IllegalStateException("ADMIN_API_KEY must be changed from default value")
        }
    }

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
     * 管理者ユーザーを更新
     */
    fun updateAdminUser(
        userId: String,
        email: String?,
        username: String?,
        password: String?
    ): AdminUserCreationResult {
        val userId = UserId(java.util.UUID.fromString(userId))
        val existingUser = userRepository.findById(userId)
            ?: throw IllegalArgumentException("ユーザーが見つかりません")

        // 管理者権限の確認
        if (existingUser.role != UserRole.ROLE_ADMIN) {
            throw IllegalArgumentException("このユーザーは管理者ユーザーではありません")
        }

        val updatedEmail = email?.let { Email(it) } ?: existingUser.email
        val updatedUsername = username?.let { Username(it) } ?: existingUser.username
        val updatedPasswordHash = password?.let { PasswordHash(passwordEncoder.encode(it)) } ?: existingUser.passwordHash

        // メールアドレスとユーザー名の重複チェック（自分自身は除外）
        if (email != null && email != existingUser.email.value && userRepository.existsByEmail(updatedEmail)) {
            throw IllegalArgumentException("このメールアドレスは既に登録されています")
        }
        if (username != null && username != existingUser.username.value && userRepository.existsByUsername(updatedUsername)) {
            throw IllegalArgumentException("このユーザー名は既に使用されています")
        }

        val updatedUser = existingUser.copy(
            email = updatedEmail,
            username = updatedUsername,
            passwordHash = updatedPasswordHash
        )

        val savedUser = userRepository.save(updatedUser)
        return AdminUserCreationResult(user = savedUser)
    }

    /**
     * 管理者ユーザーを削除
     */
    fun deleteAdminUser(userId: String) {
        val userId = UserId(java.util.UUID.fromString(userId))
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("ユーザーが見つかりません")

        // 管理者権限の確認
        if (user.role != UserRole.ROLE_ADMIN) {
            throw IllegalArgumentException("このユーザーは管理者ユーザーではありません")
        }

        userRepository.deleteById(userId)
    }

    /**
     * 管理者ユーザー一覧を取得
     */
    @Transactional(readOnly = true)
    fun getAllAdminUsers(): List<User> {
        return userRepository.findAll()
            .filter { it.role == UserRole.ROLE_ADMIN }
    }

    /**
     * 管理者ユーザーを取得
     */
    @Transactional(readOnly = true)
    fun getAdminUser(userId: String): User? {
        val userId = UserId(java.util.UUID.fromString(userId))
        val user = userRepository.findById(userId) ?: return null
        
        // 管理者権限の確認
        if (user.role != UserRole.ROLE_ADMIN) {
            return null
        }
        
        return user
    }

    /**
     * 管理者キーを検証（定数時間比較）
     */
    fun validateAdminKey(providedKey: String) {
        // 長さ制限を追加してDoS攻撃を防止
        if (providedKey.length > 1000) {
            throw IllegalArgumentException("Invalid admin API key")
        }
        
        if (adminApiKey.isBlank()) {
            throw SecurityException("Admin API key not configured")
        }

        if (!MessageDigest.isEqual(
            providedKey.toByteArray(Charsets.UTF_8),
            adminApiKey.toByteArray(Charsets.UTF_8)
        )) {
            throw SecurityException("Invalid admin API key")
        }
    }
}
