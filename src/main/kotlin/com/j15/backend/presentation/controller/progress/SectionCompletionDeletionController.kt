package com.j15.backend.presentation.controller.progress

import com.j15.backend.application.usecase.ProgressUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** セクション完了削除コントローラー 責務: セクション完了記録の削除（デバッグ用） */
@RestController
@RequestMapping("/api/progress")
class SectionCompletionDeletionController(private val progressUseCase: ProgressUseCase) {

    @DeleteMapping("/subjects/{subjectId}/sections/{sectionId}")
    fun unmarkSectionAsCleared(
            @PathVariable subjectId: Long,
            @PathVariable sectionId: Int,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Map<String, String>> {
        val userIdObj = UserId(UUID.fromString(userId))
        val subjectIdObj = SubjectId(subjectId)
        val sectionIdObj = SectionId(sectionId)

        val result = progressUseCase.unmarkSectionAsCleared(userIdObj, subjectIdObj, sectionIdObj)

        return result.fold(
                onSuccess = {
                    ResponseEntity.ok(mapOf("message" to "セクション $sectionId の完了記録を削除しました"))
                },
                onFailure = { error ->
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to (error.message ?: "削除に失敗しました")))
                }
        )
    }
}
