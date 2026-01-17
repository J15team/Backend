package com.j15.backend.application.usecase.admin

import com.j15.backend.domain.repository.assignment.AssignmentSectionRepository
import com.j15.backend.domain.repository.assignment.AssignmentSubjectRepository
import com.j15.backend.domain.repository.assignment.SubmissionRepository
import com.j15.backend.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 管理者向け進捗ダッシュボードユースケース */
@Service
@Transactional(readOnly = true)
class AdminProgressUseCase(
        private val userRepository: UserRepository,
        private val submissionRepository: SubmissionRepository,
        private val subjectRepository: AssignmentSubjectRepository,
        private val sectionRepository: AssignmentSectionRepository
) {

        /** 全ユーザーの課題題材進捗を取得 */
        fun getAllAssignmentProgress(): AdminAssignmentProgressResponse {
                val users = userRepository.findAll()
                val subjects = subjectRepository.findAll()
                val allSubmissions = submissionRepository.findAll()

                // 題材ごとの課題ありセクションを取得
                val subjectSections =
                        subjects.associate { subject ->
                                val sections = sectionRepository.findBySubjectId(subject.id)
                                subject.id.value to sections.filter { it.hasAssignment }
                        }

                val userProgresses =
                        users.map { user ->
                                val userSubmissions =
                                        allSubmissions.filter { it.userId == user.userId }

                                val subjectProgresses =
                                        subjects.map { subject ->
                                                val assignmentSections =
                                                        subjectSections[subject.id.value]
                                                                ?: emptyList()

                                                if (assignmentSections.isEmpty()) {
                                                        SubjectProgressSummary(
                                                                subjectId = subject.id.value,
                                                                title = subject.title,
                                                                progressPercent = 100,
                                                                clearedSections = 0,
                                                                totalSections = 0,
                                                                isCleared = true
                                                        )
                                                } else {
                                                        val subjectSubmissions =
                                                                userSubmissions.filter {
                                                                        it.assignmentSubjectId ==
                                                                                subject.id
                                                                }

                                                        // セクションごとの最高スコアを計算
                                                        val clearedCount =
                                                                assignmentSections.count { section
                                                                        ->
                                                                        val sectionSubmissions =
                                                                                subjectSubmissions
                                                                                        .filter {
                                                                                                it.sectionId ==
                                                                                                        section.sectionId
                                                                                        }
                                                                        val bestScore =
                                                                                sectionSubmissions
                                                                                        .maxOfOrNull {
                                                                                                it.score
                                                                                                        ?: 0
                                                                                        }
                                                                                        ?: 0
                                                                        bestScore == 100
                                                                }

                                                        val progressPercent =
                                                                (clearedCount * 100) /
                                                                        assignmentSections.size

                                                        SubjectProgressSummary(
                                                                subjectId = subject.id.value,
                                                                title = subject.title,
                                                                progressPercent = progressPercent,
                                                                clearedSections = clearedCount,
                                                                totalSections =
                                                                        assignmentSections.size,
                                                                isCleared =
                                                                        clearedCount ==
                                                                                assignmentSections
                                                                                        .size
                                                        )
                                                }
                                        }

                                UserProgressSummary(
                                        userId = user.userId.value.toString(),
                                        username = user.username.value,
                                        email = user.email.value,
                                        subjects = subjectProgresses
                                )
                        }

                return AdminAssignmentProgressResponse(users = userProgresses)
        }
}

/** 管理者向け課題進捗レスポンス */
data class AdminAssignmentProgressResponse(val users: List<UserProgressSummary>)

/** ユーザー進捗サマリー */
data class UserProgressSummary(
        val userId: String,
        val username: String,
        val email: String,
        val subjects: List<SubjectProgressSummary>
)

/** 題材進捗サマリー */
data class SubjectProgressSummary(
        val subjectId: Long,
        val title: String,
        val progressPercent: Int,
        val clearedSections: Int,
        val totalSections: Int,
        val isCleared: Boolean
)
