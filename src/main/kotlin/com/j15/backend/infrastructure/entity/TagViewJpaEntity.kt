package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * タグ閲覧記録の複合主キー
 */
data class TagViewId(
    val tagId: Long = 0L,
    val userId: UUID = UUID.randomUUID()
) : Serializable

/**
 * タグ閲覧記録JPAエンティティ
 */
@Entity
@Table(name = "tag_views")
@IdClass(TagViewId::class)
data class TagViewJpaEntity(
    @Id
    @Column(name = "tag_id")
    val tagId: Long = 0L,

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "viewed_at", nullable = false)
    val viewedAt: LocalDateTime = LocalDateTime.now()
)
