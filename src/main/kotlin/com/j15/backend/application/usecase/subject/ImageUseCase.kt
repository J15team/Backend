package com.j15.backend.application.usecase.subject

import com.j15.backend.domain.model.image.Image
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.ImageRepository
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.infrastructure.service.S3UploadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

/**
 * 画像管理ユースケース（アプリケーション層）
 */
@Service
@Transactional
class ImageUseCase(
    private val imageRepository: ImageRepository,
    private val sectionRepository: SectionRepository,
    private val s3UploadService: S3UploadService
) {
    companion object {
        private const val MAX_IMAGES_PER_SECTION = 10 // セクションあたりの最大画像数
    }

    /**
     * セクションの画像一覧を取得
     */
    @Transactional(readOnly = true)
    fun getImagesBySectionId(subjectId: SubjectId, sectionId: SectionId): List<Image> {
        // セクションの存在確認
        if (!sectionRepository.existsById(subjectId, sectionId)) {
            throw IllegalArgumentException(
                "セクションが見つかりません: subject=${subjectId.value}, section=${sectionId.value}"
            )
        }
        return imageRepository.findAllBySectionId(subjectId, sectionId)
    }

    /**
     * 画像を登録（S3アップロード + DB保存）
     */
    fun registerImage(subjectId: SubjectId, sectionId: SectionId, file: MultipartFile): Image {
        // セクションの存在確認
        if (!sectionRepository.existsById(subjectId, sectionId)) {
            throw IllegalArgumentException(
                "セクションが見つかりません: subject=${subjectId.value}, section=${sectionId.value}"
            )
        }

        // 画像数制限チェック
        val currentCount = imageRepository.countBySectionId(subjectId, sectionId)
        if (currentCount >= MAX_IMAGES_PER_SECTION) {
            throw IllegalArgumentException(
                "セクションあたりの画像数の上限（${MAX_IMAGES_PER_SECTION}枚）に達しています"
            )
        }

        // S3にアップロード
        val imageUrl = s3UploadService.uploadImage(file, folder = "section-images")

        try {
            // DB保存
            val image =
                Image(
                    imageId = null,
                    subjectId = subjectId,
                    sectionId = sectionId,
                    imageUrl = imageUrl
                )
            return imageRepository.save(image)
        } catch (e: Exception) {
            // DB保存失敗時はS3から削除（ロールバック）
            s3UploadService.deleteImage(imageUrl)
            throw e
        }
    }

    /**
     * 画像を削除（S3削除 + DB削除）
     */
    fun deleteImage(imageId: ImageId) {
        val image =
            imageRepository.findById(imageId)
                ?: throw IllegalArgumentException("画像が見つかりません: ${imageId.value}")

        // S3から削除
        s3UploadService.deleteImage(image.imageUrl)

        // DBから削除
        imageRepository.deleteById(imageId)
    }

    /**
     * セクションの全画像を削除（セクション削除時に使用）
     */
    fun deleteAllImagesBySection(subjectId: SubjectId, sectionId: SectionId) {
        val images = imageRepository.findAllBySectionId(subjectId, sectionId)

        // S3から全画像を削除
        images.forEach { image -> s3UploadService.deleteImage(image.imageUrl) }

        // DBから全画像を削除
        imageRepository.deleteAllBySectionId(subjectId, sectionId)
    }
}
