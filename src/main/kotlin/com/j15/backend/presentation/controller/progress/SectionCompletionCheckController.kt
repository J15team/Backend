package com.j15.backend.presentation.controller.progress

import com.j15.backend.application.usecase.progress.ProgressUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/** セクション完了状態確認コントローラー 責務: セクション完了状態のチェック */
@RestController
@RequestMapping("/api/progress")
class SectionCompletionCheckController(private val progressUseCase: ProgressUseCase) {

    @GetMapping("/subjects/{subjectId}/sections/{sectionId}")
    fun checkSectionCleared(
            @PathVariable subjectId: Long,
            @PathVariable sectionId: Int,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Map<String, Boolean>> {
        val userIdObj = UserId(UUID.fromString(userId))
        val subjectIdObj = SubjectId(subjectId)
        val sectionIdObj = SectionId(sectionId)
        val isCleared = progressUseCase.isSectionCleared(userIdObj, subjectIdObj, sectionIdObj)
        return ResponseEntity.ok(mapOf("isCleared" to isCleared))
    }
}
