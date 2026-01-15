package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.AssignmentSection
import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.model.assignment.TestCase
import com.j15.backend.domain.repository.AssignmentSectionRepository
import com.j15.backend.domain.repository.AssignmentSubjectRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 課題セクション管理ユースケース */
@Service
@Transactional
class AssignmentSectionUseCase(
        private val assignmentSectionRepository: AssignmentSectionRepository,
        private val assignmentSubjectRepository: AssignmentSubjectRepository
) {

    /**
     * 課題セクションを作成
     * @param assignmentSubjectId 課題題材ID
     * @param sectionId セクションID
     * @param title タイトル
     * @param description 説明（任意）
     * @param hasAssignment 課題ありフラグ
     * @param testCases テストケースJSON（hasAssignment=trueの場合必須）
     * @param timeLimit 制限時間（ミリ秒、hasAssignment=trueの場合必須）
     * @param memoryLimit メモリ制限（MB、hasAssignment=trueの場合必須）
     * @return 作成された課題セクション
     */
    fun createSection(
            assignmentSubjectId: Long,
            sectionId: Int,
            title: String,
            description: String?,
            hasAssignment: Boolean,
            testCases: String?,
            timeLimit: Int?,
            memoryLimit: Int?
    ): AssignmentSection {
        val subjectId = AssignmentSubjectId(assignmentSubjectId)

        // 課題題材の存在確認
        if (!assignmentSubjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        }

        // テストケースJSONのバリデーション
        if (hasAssignment && testCases != null) {
            TestCase.parseFromJson(testCases) // パースエラーは例外として伝播
        }

        val section =
                AssignmentSection(
                        assignmentSubjectId = subjectId,
                        sectionId = AssignmentSectionId(sectionId),
                        title = title,
                        description = description,
                        hasAssignment = hasAssignment,
                        testCases = testCases,
                        timeLimit = timeLimit,
                        memoryLimit = memoryLimit
                )

        return assignmentSectionRepository.save(section)
    }

    /**
     * 課題セクションを取得
     * @param assignmentSubjectId 課題題材ID
     * @param sectionId セクションID
     * @return 課題セクション（存在しない場合はnull）
     */
    @Transactional(readOnly = true)
    fun getSection(assignmentSubjectId: Long, sectionId: Int): AssignmentSection? {
        return assignmentSectionRepository.findById(
                AssignmentSubjectId(assignmentSubjectId),
                AssignmentSectionId(sectionId)
        )
    }

    /**
     * 課題題材の全セクションを取得
     * @param assignmentSubjectId 課題題材ID
     * @return 課題セクションリスト
     */
    @Transactional(readOnly = true)
    fun getSectionsBySubject(assignmentSubjectId: Long): List<AssignmentSection> {
        val subjectId = AssignmentSubjectId(assignmentSubjectId)

        // 課題題材の存在確認
        if (!assignmentSubjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        }

        return assignmentSectionRepository.findBySubjectId(subjectId)
    }

    /**
     * 課題セクションを更新
     * @param assignmentSubjectId 課題題材ID
     * @param sectionId セクションID
     * @param title 新しいタイトル
     * @param description 新しい説明
     * @param hasAssignment 課題ありフラグ
     * @param testCases テストケースJSON
     * @param timeLimit 制限時間（ミリ秒）
     * @param memoryLimit メモリ制限（MB）
     * @return 更新された課題セクション
     */
    fun updateSection(
            assignmentSubjectId: Long,
            sectionId: Int,
            title: String,
            description: String?,
            hasAssignment: Boolean,
            testCases: String?,
            timeLimit: Int?,
            memoryLimit: Int?
    ): AssignmentSection {
        val subjectId = AssignmentSubjectId(assignmentSubjectId)
        val secId = AssignmentSectionId(sectionId)

        // 課題題材の存在確認
        if (!assignmentSubjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        }

        // 既存セクションの存在確認
        assignmentSectionRepository.findById(subjectId, secId)
                ?: throw IllegalArgumentException("課題セクションが見つかりません: $sectionId")

        // テストケースJSONのバリデーション
        if (hasAssignment && testCases != null) {
            TestCase.parseFromJson(testCases)
        }

        val updated =
                AssignmentSection(
                        assignmentSubjectId = subjectId,
                        sectionId = secId,
                        title = title,
                        description = description,
                        hasAssignment = hasAssignment,
                        testCases = testCases,
                        timeLimit = timeLimit,
                        memoryLimit = memoryLimit
                )

        return assignmentSectionRepository.save(updated)
    }

    /**
     * 課題セクションを削除
     * @param assignmentSubjectId 課題題材ID
     * @param sectionId セクションID
     */
    fun deleteSection(assignmentSubjectId: Long, sectionId: Int) {
        val subjectId = AssignmentSubjectId(assignmentSubjectId)
        val secId = AssignmentSectionId(sectionId)

        // 課題題材の存在確認
        if (!assignmentSubjectRepository.existsById(subjectId)) {
            throw IllegalArgumentException("課題題材が見つかりません: $assignmentSubjectId")
        }

        // セクションの存在確認
        if (!assignmentSectionRepository.existsById(subjectId, secId)) {
            throw IllegalArgumentException("課題セクションが見つかりません: $sectionId")
        }

        assignmentSectionRepository.deleteById(subjectId, secId)
    }
}
