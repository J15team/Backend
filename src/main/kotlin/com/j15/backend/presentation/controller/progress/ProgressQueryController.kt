package com.j15.backend.presentation.controller.progress

import com.j15.backend.application.usecase.ProgressUseCase
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.response.UserProgressResponse
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 進捗照会コントローラー 責務: ユーザーの進捗状態の取得 */
@RestController
@RequestMapping("/api/progress")
class ProgressQueryController(private val progressUseCase: ProgressUseCase) {

    @GetMapping("/{userId}/subjects/{subjectId}")
    fun getUserProgress(
            @PathVariable userId: String,
            @PathVariable subjectId: Long
    ): ResponseEntity<UserProgressResponse> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            val subjectIdObj = SubjectId(subjectId)
            val userProgress = progressUseCase.getUserProgress(userIdObj, subjectIdObj)
            ResponseEntity.ok(UserProgressResponse.from(userProgress))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
