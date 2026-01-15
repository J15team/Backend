package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "assignment_subjects")
data class AssignmentSubjectJpaEntity(
        @Id @Column(name = "id") val id: Long = 0L,
        @Column(name = "title", nullable = false, length = 255) val title: String = "",
        @Column(name = "description", columnDefinition = "TEXT") val description: String? = null,
        @Column(name = "max_sections", nullable = false) val maxSections: Int = 0,
        @Column(name = "weight", nullable = false) val weight: Int = 1,
        @Column(name = "created_at", nullable = false)
        val createdAt: LocalDateTime = LocalDateTime.now()
)
