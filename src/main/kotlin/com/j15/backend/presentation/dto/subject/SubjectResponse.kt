package com.j15.backend.presentation.dto.subject

import com.j15.backend.domain.model.subject.Subject
import java.time.Instant

/** 題材レスポンス */
data class SubjectResponse(
        val subjectId: Long,
        val title: String,
        val description: String?,
        val maxSections: Int,
        val createdAt: Instant
) {
    companion object {
        fun from(subject: Subject): SubjectResponse {
            return SubjectResponse(
                    subjectId = subject.subjectId.value,
                    title = subject.title,
                    description = subject.description,
                    maxSections = subject.maxSections,
                    createdAt = subject.createdAt
            )
        }
    }
}
