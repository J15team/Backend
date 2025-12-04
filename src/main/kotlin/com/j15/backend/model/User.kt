package com.j15.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long? = null,

    @Column(unique = true, nullable = false, length = 20)
    val username: String,

    @Column(unique = true, nullable = false, length = 255)
    val email: String,

    @Column(nullable = false, length = 255)
    val passwordHash: String,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now()
)
