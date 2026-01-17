package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.AssignmentSubject
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.repository.assignment.AssignmentSectionRepository
import com.j15.backend.domain.repository.assignment.AssignmentSubjectRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 課題題材管理ユースケース */
@Service
@Transactional
class AssignmentSubjectUseCase(
        private val assignmentSubjectRepository: AssignmentSubjectRepository,
        private val assignmentSectionRepository: AssignmentSectionRepository
) {

    /**
     * 課題題材を作成
     * @param assignmentSubjectId 課題題材ID
     * @param title タイトル
     * @param description 説明（任意）
     * @param maxSections 最大セクション数（1-1000）
     * @param weight 重み（1-5）
     * @return 作成された課題題材
     */
    fun createSubject(
            assignmentSubjectId: Long,
            title: String,
            description: String?,
            maxSections: Int,
            weight: Int
    ): AssignmentSubject {
        validateSubjectInput(title, maxSections, weight)

        val subject =
                AssignmentSubject(
                        id = AssignmentSubjectId(assignmentSubjectId),
                        title = title,
                        description = description,
                        maxSections = maxSections,
                        weight = weight,
                        createdAt = Instant.now()
                )

        return assignmentSubjectRepository.save(subject)
    }

    /**
     * 課題題材を取得
     * @param assignmentSubjectId 課題題材ID
     * @return 課題題材（存在しない場合はnull）
     */
    @Transactional(readOnly = true)
    fun getSubject(assignmentSubjectId: Long): AssignmentSubject? {
        return assignmentSubjectRepository.findById(AssignmentSubjectId(assignmentSubjectId))
    }

    /**
     * 全課題題材を取得
     * @return 課題題材リスト
     */
    @Transactional(readOnly = true)
    fun getAllSubjects(): List<AssignmentSubject> {
        return assignmentSubjectRepository.findAll()
    }

    /**
     * 課題題材を更新
     * @param assignmentSubjectId 課題題材ID
     * @param title 新しいタイトル
     * @param description 新しい説明
     * @param maxSections 新しい最大セクション数
     * @param weight 新しい重み
     * @return 更新された課題題材
     * @throws IllegalArgumentException 課題題材が存在しない場合
     */
    fun updateSubject(
            assignmentSubjectId: Long,
            title: String,
            description: String?,
            maxSections: Int,
            weight: Int
    ): AssignmentSubject {
        val id = AssignmentSubjectId(assignmentSubjectId)
        val existing =
                assignmentSubjectRepository.findById(id)
                        ?: throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        validateSubjectInput(title, maxSections, weight)

        val updated =
                existing.copy(
                        title = title,
                        description = description,
                        maxSections = maxSections,
                        weight = weight
                )

        return assignmentSubjectRepository.save(updated)
    }

    /**
     * 課題題材を削除
     * @param assignmentSubjectId 課題題材ID
     * @throws IllegalArgumentException 課題題材が存在しない場合
     */
    fun deleteSubject(assignmentSubjectId: Long) {
        val id = AssignmentSubjectId(assignmentSubjectId)
        if (!assignmentSubjectRepository.existsById(id)) {
            throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        }
        // 関連するセクションをすべて削除
        assignmentSectionRepository.deleteAllBySubjectId(id)
        // 課題題材を削除
        assignmentSubjectRepository.deleteById(id)
    }

    private fun validateSubjectInput(title: String, maxSections: Int, weight: Int) {
        require(title.isNotBlank()) { "タイトルは必須です" }
        require(
                maxSections in
                        AssignmentSubject.MIN_MAX_SECTIONS..AssignmentSubject.MAX_MAX_SECTIONS
        ) {
            "最大セクション数は${AssignmentSubject.MIN_MAX_SECTIONS}以上${AssignmentSubject.MAX_MAX_SECTIONS}以下である必要があります"
        }
        require(weight in AssignmentSubject.MIN_WEIGHT..AssignmentSubject.MAX_WEIGHT) {
            "重みは${AssignmentSubject.MIN_WEIGHT}以上${AssignmentSubject.MAX_WEIGHT}以下である必要があります"
        }
    }
}
