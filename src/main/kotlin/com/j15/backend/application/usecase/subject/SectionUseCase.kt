package com.j15.backend.application.usecase.subject

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.infrastructure.service.S3UploadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// セクション管理ユースケース（アプリケーション層）
@Service
@Transactional
class SectionUseCase(
        private val sectionRepository: SectionRepository,
        private val subjectRepository: SubjectRepository,
        private val s3UploadService: S3UploadService
) {

    /** 特定題材の全セクション一覧を取得 フロントエンドが利用可能な全セクション情報を返す */
    @Transactional(readOnly = true)
    fun getAllSections(subjectId: SubjectId): List<Section> {
        // 題材の存在確認
        if (!subjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")
        }
        return sectionRepository.findAllBySubjectId(subjectId)
    }

    /** 特定セクションの詳細を取得 */
    @Transactional(readOnly = true)
    fun getSectionById(subjectId: SubjectId, sectionId: SectionId): Section? {
        return sectionRepository.findById(subjectId, sectionId)
    }

    /** セクションが存在するかチェック */
    @Transactional(readOnly = true)
    fun existsSection(subjectId: SubjectId, sectionId: SectionId): Boolean {
        return sectionRepository.existsById(subjectId, sectionId)
    }

    /** セクションを作成（管理者用） */
    fun createSection(section: Section): Section {
        // 題材の存在確認
        if (!subjectRepository.existsById(section.subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${section.subjectId.value}")
        }
        return sectionRepository.save(section)
    }

    /** セクションを更新（管理者用） */
    fun updateSection(
            subjectId: SubjectId,
            sectionId: SectionId,
            title: String?,
            description: String?,
            newImageUrl: String?,
            deleteImage: Boolean?
    ): Section {
        // 題材の存在確認
        if (!subjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")
        }

        // 既存セクションの取得
        val existingSection = sectionRepository.findById(subjectId, sectionId)
                ?: throw IllegalArgumentException("セクションが見つかりません: ${sectionId.value}")

        val finalImageUrl = when {
            newImageUrl != null -> {
                // 新しい画像がアップロードされた場合、既存の画像を削除してから新しい画像を保存
                existingSection.imageUrl?.let { s3UploadService.deleteImage(it) }
                newImageUrl
            }
            deleteImage == true -> {
                // 画像削除がリクエストされた場合、既存の画像を削除
                existingSection.imageUrl?.let { s3UploadService.deleteImage(it) }
                null
            }
            else -> existingSection.imageUrl // 変更なし
        }

        // 更新されたセクションを作成
        val updatedSection = existingSection.copy(
                title = title ?: existingSection.title,
                description = description ?: existingSection.description,
                imageUrl = finalImageUrl
        )

        return sectionRepository.save(updatedSection)
    }

    /** セクションを削除（管理者用） */
    fun deleteSection(subjectId: SubjectId, sectionId: SectionId) {
        // 題材の存在確認
        if (!subjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")
        }

        // セクションの存在確認
        val existingSection = sectionRepository.findById(subjectId, sectionId)
            ?: throw IllegalArgumentException("セクションが見つかりません: ${sectionId.value}")

        // 関連する画像をS3から削除
        existingSection.imageUrl?.let { s3UploadService.deleteImage(it) }

        sectionRepository.deleteById(subjectId, sectionId)
    }

    /** 指定した題材の全セクションを削除（管理者用） */
    fun deleteAllSectionsBySubjectId(subjectId: SubjectId) {
        // 題材の存在確認
        if (!subjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("題材が見つかりません: ${subjectId.value}")
        }

        // 関連する画像をS3から削除
        sectionRepository.findAllBySubjectId(subjectId).forEach { section ->
            section.imageUrl?.let { s3UploadService.deleteImage(it) }
        }

        sectionRepository.deleteAllBySubjectId(subjectId)
    }
}
