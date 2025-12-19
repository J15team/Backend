package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.subject.SectionUseCase
import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.presentation.dto.request.CreateSectionRequest
import com.j15.backend.presentation.dto.request.UpdateSectionRequest
import com.j15.backend.presentation.dto.response.SectionResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * セクション操作コントローラー
 * 責務: セクションの作成・更新・削除
 */
@RestController
@RequestMapping("/api/subjects/{subjectId}/sections")
class SectionCommandController(
        private val sectionUseCase: SectionUseCase
) {
    private val logger = LoggerFactory.getLogger(SectionCommandController::class.java)

    /**
     * セクションを作成
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createSection(
            @PathVariable subjectId: Long,
            @ModelAttribute request: CreateSectionRequest,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Any> {
        return try {
            val sectionId = request.sectionId
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to "sectionIdは必須です"))
            val title = request.title?.takeIf { it.isNotBlank() }
                    ?: return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(mapOf("error" to "titleは必須です"))

            // セクションを作成
            val section = Section(
                    subjectId = SubjectId(subjectId),
                    sectionId = SectionId(sectionId),
                    title = title,
                    description = request.description
            )

            val createdSection = sectionUseCase.createSection(section)

            ResponseEntity.status(HttpStatus.CREATED).body(SectionResponse.from(createdSection))
        } catch (e: IllegalArgumentException) {
            logger.warn("セクション作成エラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "セクションの作成中にエラーが発生しました"))
        }
    }

    /**
     * セクションを更新
     */
    @PutMapping("/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateSection(
            @PathVariable subjectId: Long,
            @PathVariable sectionId: Int,
            @ModelAttribute request: UpdateSectionRequest,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Any> {
        return try {
            // セクションを更新
            val updatedSection = sectionUseCase.updateSection(
                    subjectId = SubjectId(subjectId),
                    sectionId = SectionId(sectionId),
                    title = request.title,
                    description = request.description
            )

            ResponseEntity.ok(SectionResponse.from(updatedSection))
        } catch (e: IllegalArgumentException) {
            logger.warn("セクション更新エラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "セクションの更新中にエラーが発生しました"))
        }
    }

    /**
     * セクションを削除
     */
    @DeleteMapping("/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSection(
            @PathVariable subjectId: Long,
            @PathVariable sectionId: Int,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Void> {
        return try {
            sectionUseCase.deleteSection(
                    subjectId = SubjectId(subjectId),
                    sectionId = SectionId(sectionId)
            )
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("セクション削除エラー: ${e.message}", e)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
