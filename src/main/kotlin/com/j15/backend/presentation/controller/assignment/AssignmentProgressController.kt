package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.AssignmentProgressUseCase
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** 課題進捗コントローラー */
@RestController
@RequestMapping("/api/assignments/{assignmentSubjectId}/progress")
class AssignmentProgressController(private val progressUseCase: AssignmentProgressUseCase) {

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun getProgress(
            @PathVariable assignmentSubjectId: Long,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<AssignmentProgressResponse> {
        val progress = progressUseCase.getProgress(UUID.fromString(userId), assignmentSubjectId)

        return ResponseEntity.ok(AssignmentProgressResponse.from(progress))
    }
}

/** 進捗レスポンス */
data class AssignmentProgressResponse(
        val sections: List<SectionProgressResponse>,
        val totalSections: Int,
        val clearedSections: Int,
        val isSubjectCleared: Boolean,
        val progressPercent: Int
) {
    companion object {
        fun from(
                progress: com.j15.backend.application.usecase.assignment.AssignmentProgress
        ): AssignmentProgressResponse {
            val percent =
                    if (progress.totalSections > 0) {
                        (progress.clearedSections * 100) / progress.totalSections
                    } else 100

            return AssignmentProgressResponse(
                    sections = progress.sectionProgresses.map { SectionProgressResponse.from(it) },
                    totalSections = progress.totalSections,
                    clearedSections = progress.clearedSections,
                    isSubjectCleared = progress.isSubjectCleared,
                    progressPercent = percent
            )
        }
    }
}

/** セクション進捗レスポンス */
data class SectionProgressResponse(
        val sectionId: Int,
        val title: String,
        val bestScore: Int,
        val isCleared: Boolean,
        val submissionCount: Int
) {
    companion object {
        fun from(
                progress: com.j15.backend.application.usecase.assignment.SectionProgress
        ): SectionProgressResponse {
            return SectionProgressResponse(
                    sectionId = progress.sectionId,
                    title = progress.title,
                    bestScore = progress.bestScore,
                    isCleared = progress.isCleared,
                    submissionCount = progress.submissionCount
            )
        }
    }
}
