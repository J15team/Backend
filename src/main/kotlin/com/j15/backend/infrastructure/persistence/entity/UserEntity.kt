package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.CreationTimestamp

// ユーザーJPAエンティティ（インフラ層）
@Entity
@Table(name = "users")
class UserEntity(
        @Id @Column(name = "user_id") val userId: UUID? = null,
        @Column(unique = true, nullable = false, length = 20) var username: String,
        @Column(unique = true, nullable = false, length = 255) var email: String,
        @Column(name = "password_hash", nullable = false, length = 255)
        var passwordHash: String,
        @Column(name = "created_at", nullable = false, updatable = false)
        @CreationTimestamp
        val createdAt: Instant? = null
)
