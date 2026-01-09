package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp

/**
 * ユーザーJPAエンティティ（インフラ層）
 *
 * OAuth認証ユーザーの場合、passwordHashはnullになる
 */
@Entity
@Table(name = "users")
class UserEntity(
        @Id @Column(name = "user_id") val userId: UUID? = null,
        @Column(unique = true, nullable = false, length = 20) var username: String = "",
        @Column(unique = true, nullable = false, length = 255) var email: String = "",
        @Column(name = "password_hash", nullable = true, length = 255)
        var passwordHash: String? = null,
        @Column(nullable = false, length = 20) var role: String = "ROLE_USER",
        @Column(name = "profile_image_url", length = 2048) var profileImageUrl: String? = null,
        @Column(name = "oauth_provider", length = 50) var oauthProvider: String? = null,
        @Column(name = "oauth_provider_id", length = 255) var oauthProviderId: String? = null,
        @Column(name = "login_count", nullable = false) var loginCount: Int = 0,
        @Column(name = "last_login_at") var lastLoginAt: Instant? = null,
        @Column(name = "created_at", nullable = false, updatable = false)
        @CreationTimestamp
        val createdAt: Instant? = null
)
