package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.AssignmentSectionRepository
import com.j15.backend.domain.repository.SubmissionRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 課題進捗ユースケース */
@Service
@Transactional(readOnly = true)
class AssignmentProgressUseCase(
        private val submissionRepository: SubmissionRepository,
        private val sectionRepository: AssignmentSectionRepository
) {

    /**
     * ユーザーの題材進捗を取得
     * @return 進捗情報（セクション別クリア状況、全体クリア判定）
     */
    fun getProgress(userId: UUID, assignmentSubjectId: Long): AssignmentProgress {
        val subjectId = AssignmentSubjectId(assignmentSubjectId)
        val userIdObj = UserId(userId)

        // 題材の全セクションを取得
        val sections = sectionRepository.findBySubjectId(subjectId)

        // 課題ありセクションのみ抽出
        val assignmentSections = sections.filter { it.hasAssignment }

        if (assignmentSections.isEmpty()) {
            return AssignmentProgress(
                    sectionProgresses = emptyList(),
                    totalSections = 0,
                    clearedSections = 0,
                    isSubjectCleared = true // 課題なしなら自動クリア
            )
        }

        // ユーザーの全提出を取得
        val submissions = submissionRepository.findByUserAndSubject(userIdObj, subjectId)

        // セクションごとの最高スコアを計算
        val sectionProgresses =
                assignmentSections.map { section ->
                    val sectionSubmissions =
                            submissions.filter { it.sectionId == section.sectionId }
                    val bestScore = sectionSubmissions.maxOfOrNull { it.score ?: 0 } ?: 0
                    val isCleared = bestScore == 100

                    SectionProgress(
                            sectionId = section.sectionId.value,
                            title = section.title,
                            bestScore = bestScore,
                            isCleared = isCleared,
                            submissionCount = sectionSubmissions.size
                    )
                }

        val clearedCount = sectionProgresses.count { it.isCleared }
        val isSubjectCleared = clearedCount == assignmentSections.size

        return AssignmentProgress(
                sectionProgresses = sectionProgresses,
                totalSections = assignmentSections.size,
                clearedSections = clearedCount,
                isSubjectCleared = isSubjectCleared
        )
    }
}

/** 題材進捗 */
data class AssignmentProgress(
        val sectionProgresses: List<SectionProgress>,
        val totalSections: Int,
        val clearedSections: Int,
        val isSubjectCleared: Boolean
)

/** セクション進捗 */
data class SectionProgress(
        val sectionId: Int,
        val title: String,
        val bestScore: Int,
        val isCleared: Boolean,
        val submissionCount: Int
)
