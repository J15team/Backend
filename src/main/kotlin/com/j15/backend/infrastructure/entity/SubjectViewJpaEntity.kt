package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime
import java.util.UUID

/**
 * 題材閲覧記録の複合主キー
 */
data class SubjectViewId(
    val subjectId: Long = 0L,
    val userId: UUID = UUID.randomUUID()
) : Serializable

/**
 * 題材閲覧記録JPAエンティティ
 */
@Entity
@Table(name = "subject_views")
@IdClass(SubjectViewId::class)
data class SubjectViewJpaEntity(
    @Id
    @Column(name = "subject_id")
    val subjectId: Long = 0L,

    @Id
    @Column(name = "user_id", columnDefinition = "UUID")
    val userId: UUID = UUID.randomUUID(),

    @Column(name = "viewed_at", nullable = false)
    val viewedAt: LocalDateTime = LocalDateTime.now()
)
