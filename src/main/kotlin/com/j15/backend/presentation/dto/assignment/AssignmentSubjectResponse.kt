package com.j15.backend.presentation.dto.assignment

import com.j15.backend.domain.model.assignment.AssignmentSubject
import java.time.Instant

/** 課題題材レスポンス */
data class AssignmentSubjectResponse(
        val assignmentSubjectId: Long,
        val title: String,
        val description: String?,
        val maxSections: Int,
        val weight: Int,
        val createdAt: Instant
) {
    companion object {
        fun from(subject: AssignmentSubject): AssignmentSubjectResponse {
            return AssignmentSubjectResponse(
                    assignmentSubjectId = subject.id.value,
                    title = subject.title,
                    description = subject.description,
                    maxSections = subject.maxSections,
                    weight = subject.weight,
                    createdAt = subject.createdAt
            )
        }
    }
}
