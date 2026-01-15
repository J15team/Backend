package com.j15.backend.presentation.dto.assignment

import com.j15.backend.domain.model.assignment.Submission
import com.j15.backend.domain.model.assignment.TestResult
import java.time.Instant

/** 提出レスポンス（作成時） */
data class SubmissionCreatedResponse(val submissionId: Long, val status: String) {
    companion object {
        fun from(submission: Submission): SubmissionCreatedResponse {
            return SubmissionCreatedResponse(
                    submissionId = submission.id?.value ?: 0L,
                    status = submission.status.name
            )
        }
    }
}

/** 提出詳細レスポンス */
data class SubmissionDetailResponse(
        val submissionId: Long,
        val status: String,
        val score: Int?,
        val totalTestCases: Int?,
        val passedTestCases: Int?,
        val submittedAt: Instant,
        val results: List<TestResultResponse>?
) {
    companion object {
        fun from(
                submission: Submission,
                results: List<TestResult>? = null
        ): SubmissionDetailResponse {
            return SubmissionDetailResponse(
                    submissionId = submission.id?.value ?: 0L,
                    status = submission.status.name,
                    score = submission.score,
                    totalTestCases = submission.totalTestCases,
                    passedTestCases = submission.passedTestCases,
                    submittedAt = submission.submittedAt,
                    results = results?.map { TestResultResponse.from(it) }
            )
        }
    }
}

/** 提出履歴レスポンス */
data class SubmissionHistoryResponse(val submissions: List<SubmissionSummaryResponse>)

/** 提出サマリーレスポンス */
data class SubmissionSummaryResponse(
        val submissionId: Long,
        val status: String,
        val score: Int?,
        val submittedAt: Instant
) {
    companion object {
        fun from(submission: Submission): SubmissionSummaryResponse {
            return SubmissionSummaryResponse(
                    submissionId = submission.id?.value ?: 0L,
                    status = submission.status.name,
                    score = submission.score,
                    submittedAt = submission.submittedAt
            )
        }
    }
}

/** テスト結果レスポンス */
data class TestResultResponse(
        val index: Int,
        val verdict: String,
        val executionTime: Int?,
        val visible: Boolean,
        val actualOutput: String?
) {
    companion object {
        fun from(result: TestResult): TestResultResponse {
            return TestResultResponse(
                    index = result.testCaseIndex,
                    verdict = result.verdict.name,
                    executionTime = result.executionTime,
                    visible = result.visible,
                    actualOutput = result.actualOutput
            )
        }
    }
}
