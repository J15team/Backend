package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "tags")
data class TagJpaEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "tag_id")
        val tagId: Long = 0L,
        @Column(name = "name", nullable = false, unique = true, length = 50) val name: String = "",
        @Column(name = "type", nullable = false, length = 20) val type: String = "NORMAL",
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)
