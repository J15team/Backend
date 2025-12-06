package com.j15.backend.application.usecase

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.SubjectRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 題材管理ユースケース 題材（学習プロジェクト）のCRUD操作を提供 */
@Service
@Transactional
class SubjectUseCase(private val subjectRepository: SubjectRepository) {

    /**
     * maxSectionsパラメータのバリデーション
     * @param maxSections 検証する最大セクション数
     * @throws IllegalArgumentException バリデーションに失敗した場合
     */
    private fun validateMaxSections(maxSections: Int) {
        require(maxSections in Subject.MIN_MAX_SECTIONS..Subject.MAX_MAX_SECTIONS) {
            "最大セクション数は${Subject.MIN_MAX_SECTIONS}以上${Subject.MAX_MAX_SECTIONS}以下である必要があります"
        }
    }

    /**
     * 題材を作成
     * @param subjectId 題材ID（Long値）
     * @param title タイトル
     * @param description 説明（任意）
     * @param maxSections 最大セクション数（1-1000）
     * @return 作成された題材
     */
    fun createSubject(
            subjectId: Long,
            title: String,
            description: String?,
            maxSections: Int
    ): Subject {
        validateSubjectInput(title, maxSections)


        val subject =
                Subject(
                        subjectId = SubjectId(subjectId),
                        title = title,
                        description = description,
                        maxSections = maxSections,
                        createdAt = Instant.now()
                )

        return subjectRepository.save(subject)
    }

    /**
     * 題材を更新
     * @param subjectId 題材ID
     * @param title 新しいタイトル
     * @param description 新しい説明
     * @param maxSections 新しい最大セクション数
     * @return 更新された題材
     * @throws IllegalArgumentException 題材が存在しない場合
     */
    fun updateSubject(
            subjectId: Long,
            title: String,
            description: String?,
            maxSections: Int
    ): Subject {
        val id = SubjectId(subjectId)
        val existing =
                subjectRepository.findById(id)
                        ?: throw IllegalArgumentException("題材が見つかりません: $subjectId")
        validateSubjectInput(title, maxSections)

        val updated =
                existing.copy(title = title, description = description, maxSections = maxSections)

        return subjectRepository.save(updated)
    }

    /**
     * 題材を取得
     * @param subjectId 題材ID
     * @return 題材（存在しない場合はnull）
     */
    @Transactional(readOnly = true)
    fun getSubject(subjectId: Long): Subject? {
        return subjectRepository.findById(SubjectId(subjectId))
    }

    /**
     * 全題材を取得
     * @return 題材リスト
     */
    @Transactional(readOnly = true)
    fun getAllSubjects(): List<Subject> {
        return subjectRepository.findAll()
    }

    /**
     * 題材を削除
     * @param subjectId 題材ID
     */
    fun deleteSubject(subjectId: Long) {
        val id = SubjectId(subjectId)
        if (!subjectRepository.existsById(id)) {
            throw IllegalArgumentException("題材が見つかりません: $subjectId")
        }
        subjectRepository.deleteById(id)
    }

    /**
     * 題材の入力値を検証
     * @param title タイトル
     * @param maxSections 最大セクション数
     * @throws IllegalArgumentException バリデーションエラー時
     */
    private fun validateSubjectInput(title: String, maxSections: Int) {
        require(title.isNotBlank()) { "タイトルは必須です" }
        require(maxSections in Subject.MIN_MAX_SECTIONS..Subject.MAX_MAX_SECTIONS) {
            "最大セクション数は${Subject.MIN_MAX_SECTIONS}以上${Subject.MAX_MAX_SECTIONS}以下である必要があります"
        }
    }
}
