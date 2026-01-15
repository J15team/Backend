package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.SubmissionUseCase
import com.j15.backend.application.usecase.assignment.TestResultUseCase
import com.j15.backend.domain.model.assignment.Language
import com.j15.backend.presentation.dto.assignment.*
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** 提出コントローラー（認証必須） */
@RestController
@RequestMapping("/api/assignments/{assignmentSubjectId}/sections/{sectionId}/submissions")
class SubmissionController(
        private val submissionUseCase: SubmissionUseCase,
        private val testResultUseCase: TestResultUseCase
) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    fun submitCode(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int,
            @RequestBody request: SubmitCodeRequest,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<SubmissionCreatedResponse> {
        return try {
            val language = Language.valueOf(request.language.uppercase())
            val submission =
                    submissionUseCase.submitCode(
                            userId = UUID.fromString(userId),
                            assignmentSubjectId = assignmentSubjectId,
                            sectionId = sectionId,
                            code = request.code,
                            language = language
                    )

            ResponseEntity.status(HttpStatus.CREATED)
                    .body(SubmissionCreatedResponse.from(submission))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getSubmissionHistory(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int,
            @AuthenticationPrincipal userId: String,
            @RequestParam(required = false, defaultValue = "false") all: Boolean
    ): ResponseEntity<SubmissionHistoryResponse> {
        val submissions =
                if (all) {
                    // 管理者は全提出を取得可能（権限チェックは別途必要）
                    submissionUseCase.getAllSubmissionsBySection(assignmentSubjectId, sectionId)
                } else {
                    // 一般ユーザーは自分の提出のみ
                    submissionUseCase.getSubmissionHistory(
                            UUID.fromString(userId),
                            assignmentSubjectId,
                            sectionId
                    )
                }

        val response =
                SubmissionHistoryResponse(
                        submissions = submissions.map { SubmissionSummaryResponse.from(it) }
                )

        return ResponseEntity.ok(response)
    }

    @GetMapping("/{submissionId}")
    @PreAuthorize("isAuthenticated()")
    fun getSubmission(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int,
            @PathVariable submissionId: Long,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<SubmissionDetailResponse> {
        val submission =
                submissionUseCase.getSubmission(submissionId)
                        ?: return ResponseEntity.notFound().build()

        // 自分の提出かどうかチェック（管理者は別途権限チェック必要）
        if (submission.userId.value.toString() != userId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        // テスト結果を取得（可視性フィルタリング済み）
        val results = testResultUseCase.getFilteredResultsBySubmission(submissionId)

        return ResponseEntity.ok(SubmissionDetailResponse.from(submission, results))
    }
}
