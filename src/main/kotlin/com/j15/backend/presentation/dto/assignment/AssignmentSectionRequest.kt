package com.j15.backend.presentation.dto.assignment

/** 課題セクション作成リクエスト */
data class CreateAssignmentSectionRequest(
        val sectionId: Int,
        val title: String,
        val description: String?,
        val hasAssignment: Boolean = false,
        val testCases: String? = null,
        val timeLimit: Int? = null,
        val memoryLimit: Int? = null
)

/** 課題セクション更新リクエスト */
data class UpdateAssignmentSectionRequest(
        val title: String,
        val description: String?,
        val hasAssignment: Boolean,
        val testCases: String?,
        val timeLimit: Int?,
        val memoryLimit: Int?
)
