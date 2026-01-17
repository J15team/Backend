package com.j15.backend.application.usecase.admin

import com.j15.backend.domain.repository.assignment.AssignmentSectionRepository
import com.j15.backend.domain.repository.assignment.AssignmentSubjectRepository
import com.j15.backend.domain.repository.assignment.SubmissionRepository
import com.j15.backend.domain.repository.subject.SectionRepository
import com.j15.backend.domain.repository.subject.SubjectRepository
import com.j15.backend.domain.repository.user.UserClearedSectionRepository
import com.j15.backend.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 管理者向け進捗ダッシュボードユースケース */
@Service
@Transactional(readOnly = true)
class AdminProgressUseCase(
        private val userRepository: UserRepository,
        private val submissionRepository: SubmissionRepository,
        private val assignmentSubjectRepository: AssignmentSubjectRepository,
        private val assignmentSectionRepository: AssignmentSectionRepository,
        private val subjectRepository: SubjectRepository,
        private val sectionRepository: SectionRepository,
        private val clearedSectionRepository: UserClearedSectionRepository
) {

        /** 全ユーザーの課題題材進捗を取得 */
        fun getAllAssignmentProgress(): AdminAssignmentProgressResponse {
                val users = userRepository.findAll()
                val subjects = assignmentSubjectRepository.findAll()
                val allSubmissions = submissionRepository.findAll()

                // 題材ごとの課題ありセクションを取得
                val subjectSections =
                        subjects.associate { subject ->
                                val sections =
                                        assignmentSectionRepository.findBySubjectId(subject.id)
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
                                                        AssignmentSubjectProgressSummary(
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

                                                        AssignmentSubjectProgressSummary(
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

                                AssignmentUserProgressSummary(
                                        userId = user.userId.value.toString(),
                                        username = user.username.value,
                                        email = user.email.value,
                                        subjects = subjectProgresses
                                )
                        }

                return AdminAssignmentProgressResponse(users = userProgresses)
        }

        /** 全ユーザーの通常題材進捗を取得 */
        fun getAllSubjectProgress(): AdminSubjectProgressResponse {
                val users = userRepository.findAll()
                val subjects = subjectRepository.findAll()
                val allClearedSections = clearedSectionRepository.findAll()

                // 題材ごとのセクション数を取得
                val subjectSectionCounts =
                        subjects.associate { subject ->
                                subject.subjectId.value to
                                        sectionRepository.countBySubjectId(subject.subjectId)
                        }

                val userProgresses =
                        users.map { user ->
                                val userClearedSections =
                                        allClearedSections.filter { it.userId == user.userId }

                                val subjectProgresses =
                                        subjects.map { subject ->
                                                val totalSections =
                                                        subjectSectionCounts[
                                                                subject.subjectId.value]
                                                                ?: 0

                                                if (totalSections == 0) {
                                                        SubjectProgressSummary(
                                                                subjectId = subject.subjectId.value,
                                                                title = subject.title,
                                                                progressPercent = 100,
                                                                clearedSections = 0,
                                                                totalSections = 0,
                                                                isCleared = true
                                                        )
                                                } else {
                                                        val clearedCount =
                                                                userClearedSections.count {
                                                                        it.subjectId ==
                                                                                subject.subjectId
                                                                }

                                                        val progressPercent =
                                                                (clearedCount * 100) / totalSections

                                                        SubjectProgressSummary(
                                                                subjectId = subject.subjectId.value,
                                                                title = subject.title,
                                                                progressPercent = progressPercent,
                                                                clearedSections = clearedCount,
                                                                totalSections = totalSections,
                                                                isCleared =
                                                                        clearedCount ==
                                                                                totalSections
                                                        )
                                                }
                                        }

                                UserSubjectProgressSummary(
                                        userId = user.userId.value.toString(),
                                        username = user.username.value,
                                        email = user.email.value,
                                        subjects = subjectProgresses
                                )
                        }

                return AdminSubjectProgressResponse(users = userProgresses)
        }
}

/** 管理者向け課題進捗レスポンス */
data class AdminAssignmentProgressResponse(val users: List<AssignmentUserProgressSummary>)

/** 課題題材ユーザー進捗サマリー */
data class AssignmentUserProgressSummary(
        val userId: String,
        val username: String,
        val email: String,
        val subjects: List<AssignmentSubjectProgressSummary>
)

/** 課題題材進捗サマリー */
data class AssignmentSubjectProgressSummary(
        val subjectId: Long,
        val title: String,
        val progressPercent: Int,
        val clearedSections: Int,
        val totalSections: Int,
        val isCleared: Boolean
)

/** 管理者向け通常題材進捗レスポンス */
data class AdminSubjectProgressResponse(val users: List<UserSubjectProgressSummary>)

/** 通常題材ユーザー進捗サマリー */
data class UserSubjectProgressSummary(
        val userId: String,
        val username: String,
        val email: String,
        val subjects: List<SubjectProgressSummary>
)

/** 通常題材進捗サマリー */
data class SubjectProgressSummary(
        val subjectId: Long,
        val title: String,
        val progressPercent: Int,
        val clearedSections: Int,
        val totalSections: Int,
        val isCleared: Boolean
)
