package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

/** 題材-タグ関連の複合主キー */
data class SubjectTagId(val subjectId: Long = 0L, val tagId: Long = 0L) : Serializable

@Entity
@Table(name = "subject_tags")
@IdClass(SubjectTagId::class)
data class SubjectTagJpaEntity(
        @Id @Column(name = "subject_id") val subjectId: Long = 0L,
        @Id @Column(name = "tag_id") val tagId: Long = 0L,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)
