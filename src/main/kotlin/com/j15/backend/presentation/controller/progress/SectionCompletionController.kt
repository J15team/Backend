package com.j15.backend.presentation.controller.progress

import com.j15.backend.application.usecase.progress.ProgressUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.request.MarkSectionClearedRequest
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** セクション完了記録コントローラー 責務: セクション完了のマーク（作成） */
@RestController
@RequestMapping("/api/progress")
class SectionCompletionController(private val progressUseCase: ProgressUseCase) {

        @PostMapping("/subjects/{subjectId}/sections")
        fun markSectionAsCleared(
                @PathVariable subjectId: Long,
                @Valid @RequestBody request: MarkSectionClearedRequest,
                @AuthenticationPrincipal userId: String
        ): ResponseEntity<Map<String, Any>> {
                val userIdObj = UserId(UUID.fromString(userId))
                val subjectIdObj = SubjectId(subjectId)
                val sectionId = SectionId(request.sectionId)

                return try {
                        val clearedSection =
                                progressUseCase.markSectionAsCleared(
                                        userIdObj,
                                        subjectIdObj,
                                        sectionId
                                )

                        ResponseEntity.status(HttpStatus.CREATED)
                                .body(
                                        mapOf(
                                                "message" to
                                                        "セクション ${request.sectionId} を完了としてマークしました",
                                                "sectionId" to clearedSection.sectionId.value,
                                                "completedAt" to
                                                        clearedSection.completedAt.toString()
                                        )
                                )
                } catch (e: Exception) {
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(mapOf("error" to (e.message ?: "セクションの完了記録に失敗しました")))
                }
        }
}
