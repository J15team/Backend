package com.j15.backend.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "submissions")
data class SubmissionJpaEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        val id: Long = 0L,
        @Column(name = "user_id", nullable = false) val userId: UUID = UUID.randomUUID(),
        @Column(name = "assignment_subject_id", nullable = false)
        val assignmentSubjectId: Long = 0L,
        @Column(name = "section_id", nullable = false) val sectionId: Int = 0,
        @Column(name = "code", nullable = false, columnDefinition = "TEXT") val code: String = "",
        @Column(name = "language", nullable = false, length = 20) val language: String = "",
        @Column(name = "submitted_at", nullable = false)
        val submittedAt: LocalDateTime = LocalDateTime.now(),
        @Column(name = "status", nullable = false, length = 20) val status: String = "PENDING",
        @Column(name = "score") val score: Int? = null,
        @Column(name = "total_test_cases") val totalTestCases: Int? = null,
        @Column(name = "passed_test_cases") val passedTestCases: Int? = null
)
