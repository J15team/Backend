package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "subjects")
data class SubjectJpaEntity(
        @Id @Column(name = "subject_id") val subjectId: Long = 0L,
        @Column(name = "title", nullable = false, length = 255) val title: String = "",
        @Column(name = "description", columnDefinition = "TEXT") val description: String? = null,
        @Column(name = "max_sections", nullable = false) val maxSections: Int = 0,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)
