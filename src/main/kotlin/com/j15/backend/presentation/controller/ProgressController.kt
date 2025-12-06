package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.ProgressUseCase
import com.j15.backend.application.usecase.SectionUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.request.MarkSectionClearedRequest
import com.j15.backend.presentation.dto.response.UserProgressResponse
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/progress")
class ProgressController(
        private val progressUseCase: ProgressUseCase,
        private val sectionUseCase: SectionUseCase
) {

        /** ユーザーの題材ごと進捗状態を取得 ログイン後、フロントエンドの進捗バーに表示するためのデータを返す */
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

        /** セクション完了をマーク フロントエンドからセクション完了通知を受け取り、DBに記録 */
        @PostMapping("/{userId}/subjects/{subjectId}/sections")
        fun markSectionAsCleared(
                @PathVariable userId: String,
                @PathVariable subjectId: Long,
                @Valid @RequestBody request: MarkSectionClearedRequest
        ): ResponseEntity<Map<String, Any>> {
                val userIdObj = UserId(UUID.fromString(userId))
                val subjectIdObj = SubjectId(subjectId)
                val sectionId = SectionId(request.sectionId)

                val result =
                        progressUseCase.markSectionAsCleared(userIdObj, subjectIdObj, sectionId)

                return result.fold(
                        onSuccess = { clearedSection ->
                                ResponseEntity.status(HttpStatus.CREATED)
                                        .body(
                                                mapOf(
                                                        "message" to
                                                                "セクション ${request.sectionId} を完了としてマークしました",
                                                        "sectionId" to
                                                                clearedSection.sectionId.value,
                                                        "completedAt" to
                                                                clearedSection.completedAt
                                                                        .toString()
                                                )
                                        )
                        },
                        onFailure = { error ->
                                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(
                                                mapOf(
                                                        "error" to
                                                                (error.message
                                                                        ?: "セクションの完了記録に失敗しました")
                                                )
                                        )
                        }
                )
        }

        /** 特定セクションの完了状態をチェック */
        @GetMapping("/{userId}/subjects/{subjectId}/sections/{sectionId}")
        fun checkSectionCleared(
                @PathVariable userId: String,
                @PathVariable subjectId: Long,
                @PathVariable sectionId: Int
        ): ResponseEntity<Map<String, Boolean>> {
                val userIdObj = UserId(UUID.fromString(userId))
                val subjectIdObj = SubjectId(subjectId)
                val sectionIdObj = SectionId(sectionId)
                val isCleared =
                        progressUseCase.isSectionCleared(userIdObj, subjectIdObj, sectionIdObj)
                return ResponseEntity.ok(mapOf("isCleared" to isCleared))
        }

        /** セクション完了を取り消し（デバッグ用） */
        @DeleteMapping("/{userId}/subjects/{subjectId}/sections/{sectionId}")
        fun unmarkSectionAsCleared(
                @PathVariable userId: String,
                @PathVariable subjectId: Long,
                @PathVariable sectionId: Int
        ): ResponseEntity<Map<String, String>> {
                val userIdObj = UserId(UUID.fromString(userId))
                val subjectIdObj = SubjectId(subjectId)
                val sectionIdObj = SectionId(sectionId)

                val result =
                        progressUseCase.unmarkSectionAsCleared(
                                userIdObj,
                                subjectIdObj,
                                sectionIdObj
                        )

                return result.fold(
                        onSuccess = {
                                ResponseEntity.ok(
                                        mapOf("message" to "セクション $sectionId の完了記録を削除しました")
                                )
                        },
                        onFailure = { error ->
                                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                        .body(mapOf("error" to (error.message ?: "削除に失敗しました")))
                        }
                )
        }
}
