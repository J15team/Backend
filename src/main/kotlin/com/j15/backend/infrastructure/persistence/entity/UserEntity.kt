package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant

/** ユーザーJPAエンティティ（インフラ層） データベースとのマッピング用エンティティ */
@Entity
@Table(name = "users")
class UserEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "user_id")
        val userId: Long? = null,
        @Column(unique = true, nullable = false, length = 20) val username: String,
        @Column(unique = true, nullable = false, length = 255) val email: String,
        @Column(name = "password_hash", nullable = false, length = 255) val passwordHash: String,
        @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
        val createdAt: Instant? = null
)
