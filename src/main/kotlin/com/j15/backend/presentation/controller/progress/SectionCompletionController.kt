package com.j15.backend.presentation.controller.progress

import com.j15.backend.application.usecase.ProgressUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.request.MarkSectionClearedRequest
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** セクション完了記録コントローラー 責務: セクション完了のマーク（作成） */
@RestController
@RequestMapping("/api/progress")
class SectionCompletionController(private val progressUseCase: ProgressUseCase) {

    @PostMapping("/{userId}/subjects/{subjectId}/sections")
    fun markSectionAsCleared(
            @PathVariable userId: String,
            @PathVariable subjectId: Long,
            @Valid @RequestBody request: MarkSectionClearedRequest
    ): ResponseEntity<Map<String, Any>> {
        val userIdObj = UserId(UUID.fromString(userId))
        val subjectIdObj = SubjectId(subjectId)
        val sectionId = SectionId(request.sectionId)

        val result = progressUseCase.markSectionAsCleared(userIdObj, subjectIdObj, sectionId)

        return result.fold(
                onSuccess = { clearedSection ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(
                                    mapOf(
                                            "message" to "セクション ${request.sectionId} を完了としてマークしました",
                                            "sectionId" to clearedSection.sectionId.value,
                                            "completedAt" to clearedSection.completedAt.toString()
                                    )
                            )
                },
                onFailure = { error ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to (error.message ?: "セクションの完了記録に失敗しました")))
                }
        )
    }
}
