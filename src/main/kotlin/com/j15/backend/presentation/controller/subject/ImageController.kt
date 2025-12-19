package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.subject.ImageUseCase
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.presentation.dto.response.ImageResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/**
 * 画像管理コントローラー
 * 責務: セクションに関連付けられた画像の登録・削除・取得
 */
@RestController
@RequestMapping("/api/subjects/{subjectId}/sections/{sectionId}/images")
class ImageController(private val imageUseCase: ImageUseCase) {
    private val logger = LoggerFactory.getLogger(ImageController::class.java)

    /**
     * セクションの画像一覧を取得
     */
    @GetMapping
    fun getImages(
        @PathVariable subjectId: Long,
        @PathVariable sectionId: Int
    ): ResponseEntity<Any> {
        return try {
            val images =
                imageUseCase.getImagesBySectionId(SubjectId(subjectId), SectionId(sectionId))
            val response = images.map { ImageResponse.from(it) }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("画像取得エラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "画像の取得中にエラーが発生しました"))
        }
    }

    /**
     * 画像を登録（S3アップロード + DB保存）
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun uploadImage(
        @PathVariable subjectId: Long,
        @PathVariable sectionId: Int,
        @RequestParam("image") file: MultipartFile,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<Any> {
        return try {
            val image =
                imageUseCase.registerImage(
                    SubjectId(subjectId),
                    SectionId(sectionId),
                    file
                )
            ResponseEntity.status(HttpStatus.CREATED).body(ImageResponse.from(image))
        } catch (e: IllegalArgumentException) {
            logger.warn("画像アップロードエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "画像のアップロード中にエラーが発生しました"))
        }
    }

    /**
     * 画像を削除（S3削除 + DB削除）
     */
    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteImage(
        @PathVariable subjectId: Long,
        @PathVariable sectionId: Int,
        @PathVariable imageId: Long,
        @AuthenticationPrincipal userId: String
    ): ResponseEntity<Void> {
        return try {
            imageUseCase.deleteImage(ImageId(imageId))
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("画像削除エラー: ${e.message}", e)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("予期しないエラー: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
