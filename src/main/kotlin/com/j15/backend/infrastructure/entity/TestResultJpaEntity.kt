package com.j15.backend.infrastructure.entity

import jakarta.persistence.*

@Entity
@Table(name = "test_results")
data class TestResultJpaEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id")
        val id: Long = 0L,
        @Column(name = "submission_id", nullable = false) val submissionId: Long = 0L,
        @Column(name = "test_case_index", nullable = false) val testCaseIndex: Int = 0,
        @Column(name = "verdict", nullable = false, length = 10) val verdict: String = "",
        @Column(name = "execution_time") val executionTime: Int? = null,
        @Column(name = "memory_used") val memoryUsed: Int? = null,
        @Column(name = "visible", nullable = false) val visible: Boolean = false,
        @Column(name = "actual_output", columnDefinition = "TEXT") val actualOutput: String? = null,
        @Column(name = "error_message", columnDefinition = "TEXT") val errorMessage: String? = null
)
