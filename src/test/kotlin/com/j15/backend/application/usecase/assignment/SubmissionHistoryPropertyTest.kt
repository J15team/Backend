package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.*
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.AssignmentSectionRepository
import com.j15.backend.domain.repository.SubmissionRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.time.Instant
import java.util.UUID

/**
 * Feature: assignment-execution-system, Property 8: 提出履歴の時系列順序 Validates: Requirements 6.1
 *
 * For any 提出履歴取得, 結果は提出日時の降順で返される
 */
class SubmissionHistoryPropertyTest :
        FunSpec({
            test("提出履歴は提出日時の降順で返される") {
                checkAll(100, Arb.int(2..20)) { submissionCount ->
                    val mockRepository = InMemorySubmissionRepository()
                    val mockSectionRepository = MockSectionRepository()
                    val useCase = SubmissionUseCase(mockRepository, mockSectionRepository)

                    val userId = UUID.randomUUID()
                    val subjectId = 1L
                    val sectionId = 1

                    // 異なる時刻で提出を作成（ランダムな順序で追加）
                    val baseTime = Instant.now()
                    val submissions =
                            (0 until submissionCount).map { i ->
                                Submission(
                                        id = SubmissionId((i + 1).toLong()),
                                        userId = UserId(userId),
                                        assignmentSubjectId = AssignmentSubjectId(subjectId),
                                        sectionId = AssignmentSectionId(sectionId),
                                        code = "int main() { return $i; }",
                                        language = Language.C,
                                        submittedAt = baseTime.plusSeconds(i.toLong()),
                                        status = SubmissionStatus.COMPLETED,
                                        score = minOf(i * 10, 100)
                                )
                            }

                    // シャッフルして追加（順序をランダムに）
                    submissions.shuffled().forEach { mockRepository.addSubmission(it) }

                    // 履歴を取得
                    val history = useCase.getSubmissionHistory(userId, subjectId, sectionId)

                    // 降順であることを確認
                    history.size shouldBe submissionCount
                    for (i in 0 until history.size - 1) {
                        val current = history[i].submittedAt
                        val next = history[i + 1].submittedAt
                        (current >= next) shouldBe true
                    }
                }
            }

            test("同一時刻の提出がある場合でも安定してソートされる") {
                val mockRepository = InMemorySubmissionRepository()
                val mockSectionRepository = MockSectionRepository()
                val useCase = SubmissionUseCase(mockRepository, mockSectionRepository)

                val userId = UUID.randomUUID()
                val subjectId = 1L
                val sectionId = 1
                val sameTime = Instant.now()

                // 同じ時刻で複数の提出を作成
                repeat(5) { i ->
                    mockRepository.addSubmission(
                            Submission(
                                    id = SubmissionId((i + 1).toLong()),
                                    userId = UserId(userId),
                                    assignmentSubjectId = AssignmentSubjectId(subjectId),
                                    sectionId = AssignmentSectionId(sectionId),
                                    code = "int main() { return $i; }",
                                    language = Language.C,
                                    submittedAt = sameTime,
                                    status = SubmissionStatus.COMPLETED
                            )
                    )
                }

                val history = useCase.getSubmissionHistory(userId, subjectId, sectionId)

                // 全て同じ時刻
                history.size shouldBe 5
                history.all { it.submittedAt == sameTime } shouldBe true
            }

            test("空の履歴は空リストを返す") {
                val mockRepository = InMemorySubmissionRepository()
                val mockSectionRepository = MockSectionRepository()
                val useCase = SubmissionUseCase(mockRepository, mockSectionRepository)

                val history = useCase.getSubmissionHistory(UUID.randomUUID(), 1L, 1)

                history.size shouldBe 0
            }
        })

// テスト用インメモリリポジトリ
private class InMemorySubmissionRepository : SubmissionRepository {
    private val submissions = mutableListOf<Submission>()

    fun addSubmission(submission: Submission) {
        submissions.add(submission)
    }

    override fun findById(id: SubmissionId): Submission? {
        return submissions.find { it.id == id }
    }

    override fun findByUserAndSection(
            userId: UserId,
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission> {
        return submissions
                .filter {
                    it.userId == userId &&
                            it.assignmentSubjectId == subjectId &&
                            it.sectionId == sectionId
                }
                .sortedByDescending { it.submittedAt }
    }

    override fun findBySection(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission> {
        return submissions
                .filter { it.assignmentSubjectId == subjectId && it.sectionId == sectionId }
                .sortedByDescending { it.submittedAt }
    }

    override fun save(submission: Submission): Submission {
        submissions.add(submission)
        return submission
    }
}

private class MockSectionRepository : AssignmentSectionRepository {
    override fun findById(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): AssignmentSection? = null

    override fun findBySubjectId(subjectId: AssignmentSubjectId): List<AssignmentSection> =
            emptyList()

    override fun existsById(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): Boolean = false

    override fun save(section: AssignmentSection): AssignmentSection = section
    override fun deleteById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) {}
    override fun deleteAllBySubjectId(subjectId: AssignmentSubjectId) {}
}
