package com.j15.backend.presentation.controller.ranking

import com.j15.backend.application.usecase.ranking.RankingUseCase
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.ranking.ViewRecordResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 閲覧記録コントローラー
 * 責務: 題材・タグの閲覧記録の作成
 */
@RestController
@RequestMapping("/api/views")
class ViewRecordController(
    private val rankingUseCase: RankingUseCase
) {
    /**
     * 題材の閲覧を記録する
     * 同じユーザーからは1回のみカウントされる
     */
    @PostMapping("/subjects/{subjectId}")
    fun recordSubjectView(
        @PathVariable subjectId: Long,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<ViewRecordResponse> {
        return try {
            val userIdObj = UserId(userId)
            val isNewView = rankingUseCase.recordSubjectView(subjectId, userIdObj)

            val response = if (isNewView) {
                ViewRecordResponse.newView()
            } else {
                ViewRecordResponse.alreadyViewed()
            }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * タグの閲覧を記録する
     * 同じユーザーからは1回のみカウントされる
     */
    @PostMapping("/tags/{tagId}")
    fun recordTagView(
        @PathVariable tagId: Long,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<ViewRecordResponse> {
        return try {
            val userIdObj = UserId(userId)
            val isNewView = rankingUseCase.recordTagView(tagId, userIdObj)

            val response = if (isNewView) {
                ViewRecordResponse.newView()
            } else {
                ViewRecordResponse.alreadyViewed()
            }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
