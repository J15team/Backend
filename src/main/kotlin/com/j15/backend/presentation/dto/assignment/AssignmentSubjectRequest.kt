package com.j15.backend.presentation.dto.assignment

/** 課題題材作成リクエスト */
data class CreateAssignmentSubjectRequest(
        val assignmentSubjectId: Long,
        val title: String,
        val description: String?,
        val maxSections: Int,
        val weight: Int = 1
)

/** 課題題材更新リクエスト */
data class UpdateAssignmentSubjectRequest(
        val title: String,
        val description: String?,
        val maxSections: Int,
        val weight: Int
)
