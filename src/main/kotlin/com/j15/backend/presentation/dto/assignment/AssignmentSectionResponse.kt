package com.j15.backend.presentation.dto.assignment

import com.j15.backend.domain.model.assignment.AssignmentSection
import com.j15.backend.domain.model.assignment.TestCase

/** 課題セクションレスポンス */
data class AssignmentSectionResponse(
        val assignmentSubjectId: Long,
        val sectionId: Int,
        val title: String,
        val description: String?,
        val hasAssignment: Boolean,
        val timeLimit: Int?,
        val memoryLimit: Int?
) {
    companion object {
        fun from(section: AssignmentSection): AssignmentSectionResponse {
            return AssignmentSectionResponse(
                    assignmentSubjectId = section.assignmentSubjectId.value,
                    sectionId = section.sectionId.value,
                    title = section.title,
                    description = section.description,
                    hasAssignment = section.hasAssignment,
                    timeLimit = section.timeLimit,
                    memoryLimit = section.memoryLimit
            )
        }
    }
}

/** 課題セクション詳細レスポンス（テストケース含む） */
data class AssignmentSectionDetailResponse(
        val assignmentSubjectId: Long,
        val sectionId: Int,
        val title: String,
        val description: String?,
        val hasAssignment: Boolean,
        val testCases: List<TestCaseResponse>?,
        val timeLimit: Int?,
        val memoryLimit: Int?
) {
    companion object {
        fun from(section: AssignmentSection): AssignmentSectionDetailResponse {
            val testCases =
                    section.testCases?.let { json ->
                        try {
                            TestCase.parseFromJson(json).map { TestCaseResponse.from(it) }
                        } catch (e: Exception) {
                            null
                        }
                    }

            return AssignmentSectionDetailResponse(
                    assignmentSubjectId = section.assignmentSubjectId.value,
                    sectionId = section.sectionId.value,
                    title = section.title,
                    description = section.description,
                    hasAssignment = section.hasAssignment,
                    testCases = testCases,
                    timeLimit = section.timeLimit,
                    memoryLimit = section.memoryLimit
            )
        }
    }
}

/** テストケースレスポンス */
data class TestCaseResponse(val input: String, val expected: String, val visible: Boolean) {
    companion object {
        fun from(testCase: TestCase): TestCaseResponse {
            return TestCaseResponse(
                    input = testCase.input,
                    expected = testCase.expected,
                    visible = testCase.visible
            )
        }
    }
}
