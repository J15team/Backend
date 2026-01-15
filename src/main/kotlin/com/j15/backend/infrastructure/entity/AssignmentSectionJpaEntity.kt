package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.io.Serializable

/** 課題セクションの複合主キークラス */
@Embeddable
data class AssignmentSectionEntityId(
        @Column(name = "assignment_subject_id") val assignmentSubjectId: Long = 0L,
        @Column(name = "section_id") val sectionId: Int = 0
) : Serializable

@Entity
@Table(name = "assignment_sections")
@IdClass(AssignmentSectionEntityId::class)
data class AssignmentSectionJpaEntity(
        @Id
        @Column(name = "assignment_subject_id", nullable = false)
        val assignmentSubjectId: Long = 0L,
        @Id @Column(name = "section_id", nullable = false) val sectionId: Int = 0,
        @Column(name = "title", nullable = false, length = 255) val title: String = "",
        @Column(name = "description", columnDefinition = "TEXT") val description: String? = null,
        @Column(name = "has_assignment", nullable = false) val hasAssignment: Boolean = false,
        @Column(name = "test_cases", columnDefinition = "JSONB") val testCases: String? = null,
        @Column(name = "time_limit") val timeLimit: Int? = null,
        @Column(name = "memory_limit") val memoryLimit: Int? = null
)
