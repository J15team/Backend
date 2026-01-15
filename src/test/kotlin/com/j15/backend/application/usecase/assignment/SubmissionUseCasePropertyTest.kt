package com.j15.backend.application.usecase.assignment

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/**
 * Feature: assignment-execution-system, Property 5: 部分点計算の正確性 Validates: Requirements 5.2
 *
 * For any 判定結果, 部分点 = (通過テストケース数 / 全テストケース数) × 100 で計算される
 */
class SubmissionUseCasePropertyTest :
        FunSpec({
            test("部分点は (通過テストケース数 / 全テストケース数) × 100 で計算される") {
                val useCase =
                        SubmissionUseCase(
                                submissionRepository = MockSubmissionRepository(),
                                assignmentSectionRepository = MockAssignmentSectionRepository()
                        )

                checkAll(100, Arb.int(1..100), Arb.int(0..100)) { total, passed ->
                    val actualPassed = minOf(passed, total) // passedがtotalを超えないように
                    val expectedScore = (actualPassed * 100) / total

                    val score = useCase.calculateScore(actualPassed, total)

                    score shouldBe expectedScore
                }
            }

            test("全テストケース通過時は100点") {
                val useCase =
                        SubmissionUseCase(
                                submissionRepository = MockSubmissionRepository(),
                                assignmentSectionRepository = MockAssignmentSectionRepository()
                        )

                checkAll(100, Arb.int(1..100)) { total ->
                    val score = useCase.calculateScore(total, total)
                    score shouldBe 100
                }
            }

            test("全テストケース失敗時は0点") {
                val useCase =
                        SubmissionUseCase(
                                submissionRepository = MockSubmissionRepository(),
                                assignmentSectionRepository = MockAssignmentSectionRepository()
                        )

                checkAll(100, Arb.int(1..100)) { total ->
                    val score = useCase.calculateScore(0, total)
                    score shouldBe 0
                }
            }

            test("テストケースが0件の場合は0点") {
                val useCase =
                        SubmissionUseCase(
                                submissionRepository = MockSubmissionRepository(),
                                assignmentSectionRepository = MockAssignmentSectionRepository()
                        )

                val score = useCase.calculateScore(0, 0)
                score shouldBe 0
            }

            test("部分点は整数除算で計算される（切り捨て）") {
                val useCase =
                        SubmissionUseCase(
                                submissionRepository = MockSubmissionRepository(),
                                assignmentSectionRepository = MockAssignmentSectionRepository()
                        )

                // 1/3 = 33.33... → 33
                useCase.calculateScore(1, 3) shouldBe 33

                // 2/3 = 66.66... → 66
                useCase.calculateScore(2, 3) shouldBe 66

                // 1/7 = 14.28... → 14
                useCase.calculateScore(1, 7) shouldBe 14
            }
        })

// テスト用モックリポジトリ
private class MockSubmissionRepository : com.j15.backend.domain.repository.SubmissionRepository {
    override fun findById(id: com.j15.backend.domain.model.assignment.SubmissionId) = null
    override fun findByUserAndSection(
            userId: com.j15.backend.domain.model.user.UserId,
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = emptyList<com.j15.backend.domain.model.assignment.Submission>()
    override fun findBySection(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = emptyList<com.j15.backend.domain.model.assignment.Submission>()
    override fun save(submission: com.j15.backend.domain.model.assignment.Submission) = submission
}

private class MockAssignmentSectionRepository :
        com.j15.backend.domain.repository.AssignmentSectionRepository {
    override fun findById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = null
    override fun findBySubjectId(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId
    ) = emptyList<com.j15.backend.domain.model.assignment.AssignmentSection>()
    override fun existsById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = false
    override fun save(section: com.j15.backend.domain.model.assignment.AssignmentSection) = section
    override fun deleteById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) {}
    override fun deleteAllBySubjectId(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId
    ) {}
}

/**
 * Feature: assignment-execution-system, Property 4: 再提出時の新規レコード作成 Validates: Requirements 3.4
 *
 * For any 同一ユーザー・同一セクションへの再提出, 既存レコードを更新せず新しいレコードが作成される
 *
 * Note: このプロパティはSubmissionPropertyTestでドメインモデルレベルで検証済み。 ここではUseCaseレベルでの振る舞いを確認。
 */
class ResubmissionPropertyTest :
        FunSpec({
            test("再提出時は新しいSubmissionが作成される（既存は変更されない）") {
                val mockRepository = TrackingSubmissionRepository()
                val mockSectionRepository = MockAssignmentSectionRepositoryWithAssignment()
                val useCase = SubmissionUseCase(mockRepository, mockSectionRepository)

                checkAll(100, Arb.int(2..10)) { submissionCount ->
                    mockRepository.clear()
                    val userId = java.util.UUID.randomUUID()

                    // 複数回提出
                    repeat(submissionCount) {
                        useCase.submitCode(
                                userId = userId,
                                assignmentSubjectId = 1L,
                                sectionId = 1,
                                code = "int main() { return $it; }",
                                language = com.j15.backend.domain.model.assignment.Language.C
                        )
                    }

                    // 全ての提出が保存されている
                    mockRepository.savedCount shouldBe submissionCount
                }
            }
        })

// 保存回数を追跡するモックリポジトリ
private class TrackingSubmissionRepository :
        com.j15.backend.domain.repository.SubmissionRepository {
    private var _savedCount = 0
    val savedCount: Int
        get() = _savedCount

    fun clear() {
        _savedCount = 0
    }

    override fun findById(id: com.j15.backend.domain.model.assignment.SubmissionId) = null
    override fun findByUserAndSection(
            userId: com.j15.backend.domain.model.user.UserId,
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = emptyList<com.j15.backend.domain.model.assignment.Submission>()
    override fun findBySection(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = emptyList<com.j15.backend.domain.model.assignment.Submission>()
    override fun save(
            submission: com.j15.backend.domain.model.assignment.Submission
    ): com.j15.backend.domain.model.assignment.Submission {
        _savedCount++
        return submission.copy(
                id = com.j15.backend.domain.model.assignment.SubmissionId(_savedCount.toLong())
        )
    }
}

// 課題ありセクションを返すモックリポジトリ
private class MockAssignmentSectionRepositoryWithAssignment :
        com.j15.backend.domain.repository.AssignmentSectionRepository {
    override fun findById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) =
            com.j15.backend.domain.model.assignment.AssignmentSection(
                    assignmentSubjectId = subjectId,
                    sectionId = sectionId,
                    title = "Test Section",
                    hasAssignment = true,
                    testCases = """[{"input":"","expected":"Hello\n","visible":true}]""",
                    timeLimit = 2000,
                    memoryLimit = 256
            )
    override fun findBySubjectId(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId
    ) = emptyList<com.j15.backend.domain.model.assignment.AssignmentSection>()
    override fun existsById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) = true
    override fun save(section: com.j15.backend.domain.model.assignment.AssignmentSection) = section
    override fun deleteById(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId,
            sectionId: com.j15.backend.domain.model.assignment.AssignmentSectionId
    ) {}
    override fun deleteAllBySubjectId(
            subjectId: com.j15.backend.domain.model.assignment.AssignmentSubjectId
    ) {}
}
