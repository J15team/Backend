package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.*
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.AssignmentSectionRepository
import com.j15.backend.domain.repository.SubmissionRepository
import com.j15.backend.infrastructure.client.JudgeServiceClient
import com.j15.backend.infrastructure.client.JudgeServiceUnavailableException
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 提出管理ユースケース */
@Service
@Transactional
class SubmissionUseCase(
        private val submissionRepository: SubmissionRepository,
        private val assignmentSectionRepository: AssignmentSectionRepository,
        private val judgeServiceClient: JudgeServiceClient
) {
        private val logger = LoggerFactory.getLogger(SubmissionUseCase::class.java)

        /**
         * コードを提出
         * @param userId ユーザーID
         * @param assignmentSubjectId 課題題材ID
         * @param sectionId セクションID
         * @param code 提出コード
         * @param language 言語
         * @return 作成された提出
         * @throws IllegalArgumentException セクションが存在しない、または課題なしセクションの場合
         */
        fun submitCode(
                userId: UUID,
                assignmentSubjectId: Long,
                sectionId: Int,
                code: String,
                language: Language
        ): Submission {
                val subjectId = AssignmentSubjectId(assignmentSubjectId)
                val secId = AssignmentSectionId(sectionId)

                // セクションの存在確認と課題有無チェック
                val section =
                        assignmentSectionRepository.findById(subjectId, secId)
                                ?: throw IllegalArgumentException("課題セクションが見つかりません")

                if (!section.hasAssignment) {
                        throw IllegalArgumentException("このセクションには課題がありません")
                }

                // テストケースをパース
                val testCases = TestCase.parseFromJson(section.testCases!!)

                // Judge Serviceで判定
                val (status, score, passedCount) =
                        try {
                                val results =
                                        judgeServiceClient.judge(
                                                code = code,
                                                language = language,
                                                testCases = testCases,
                                                timeLimit = section.timeLimit
                                                                ?: AssignmentSection
                                                                        .DEFAULT_TIME_LIMIT,
                                                memoryLimit = section.memoryLimit
                                                                ?: AssignmentSection
                                                                        .DEFAULT_MEMORY_LIMIT
                                        )

                                val passed = results.count { it.verdict == Verdict.AC }
                                val total = results.size
                                val calculatedScore = calculateScore(passed, total)
                                val finalStatus = SubmissionStatus.COMPLETED

                                Triple(finalStatus, calculatedScore, passed)
                        } catch (e: JudgeServiceUnavailableException) {
                                logger.error("Judge Service unavailable", e)
                                Triple(SubmissionStatus.PENDING, null, null)
                        }

                // 提出を作成
                val submission =
                        Submission(
                                id = null,
                                userId = UserId(userId),
                                assignmentSubjectId = subjectId,
                                sectionId = secId,
                                code = code,
                                language = language,
                                submittedAt = Instant.now(),
                                status = status,
                                score = score,
                                totalTestCases = testCases.size,
                                passedTestCases = passedCount
                        )

                return submissionRepository.save(submission)
        }

        /**
         * 提出を取得
         * @param submissionId 提出ID
         * @return 提出（存在しない場合はnull）
         */
        @Transactional(readOnly = true)
        fun getSubmission(submissionId: Long): Submission? {
                return submissionRepository.findById(SubmissionId(submissionId))
        }

        /**
         * ユーザーの提出履歴を取得（時系列降順）
         * @param userId ユーザーID
         * @param assignmentSubjectId 課題題材ID
         * @param sectionId セクションID
         * @return 提出リスト（新しい順）
         */
        @Transactional(readOnly = true)
        fun getSubmissionHistory(
                userId: UUID,
                assignmentSubjectId: Long,
                sectionId: Int
        ): List<Submission> {
                return submissionRepository.findByUserAndSection(
                        UserId(userId),
                        AssignmentSubjectId(assignmentSubjectId),
                        AssignmentSectionId(sectionId)
                )
        }

        /**
         * セクションの全提出を取得（管理者用）
         * @param assignmentSubjectId 課題題材ID
         * @param sectionId セクションID
         * @return 提出リスト
         */
        @Transactional(readOnly = true)
        fun getAllSubmissionsBySection(
                assignmentSubjectId: Long,
                sectionId: Int
        ): List<Submission> {
                return submissionRepository.findBySection(
                        AssignmentSubjectId(assignmentSubjectId),
                        AssignmentSectionId(sectionId)
                )
        }

        /**
         * 部分点を計算
         * @param passedTestCases 通過テストケース数
         * @param totalTestCases 全テストケース数
         * @return 部分点（0-100）
         */
        fun calculateScore(passedTestCases: Int, totalTestCases: Int): Int {
                if (totalTestCases == 0) return 0
                return (passedTestCases * 100) / totalTestCases
        }
}
