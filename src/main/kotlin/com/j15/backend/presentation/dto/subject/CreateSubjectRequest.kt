package com.j15.backend.presentation.dto.subject

/** 題材作成リクエスト */
data class CreateSubjectRequest(
        val subjectId: Long,
        val title: String,
        val description: String?,
        val maxSections: Int
)
