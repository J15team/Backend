package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.Language
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

/** Feature: assignment-execution-system, Property 5: 部分点計算の正確性 Validates: Requirements 5.2 */
class SubmissionUseCasePropertyTest :
        FunSpec({
                test("部分点は (通過テストケース数 / 全テストケース数) × 100 で計算される") {
                        val useCase =
                                SubmissionUseCase(
                                        submissionRepository = TestMockSubmissionRepository(),
                                        assignmentSectionRepository =
                                                TestMockAssignmentSectionRepository(),
                                        judgeServiceClient = TestMockJudgeServiceClient()
                                )

                        checkAll(100, Arb.int(1..100), Arb.int(0..100)) { total, passed ->
                                val actualPassed = minOf(passed, total)
                                val expectedScore = (actualPassed * 100) / total
                                val score = useCase.calculateScore(actualPassed, total)
                                score shouldBe expectedScore
                        }
                }

                test("全テストケース通過時は100点") {
                        val useCase =
                                SubmissionUseCase(
                                        submissionRepository = TestMockSubmissionRepository(),
                                        assignmentSectionRepository =
                                                TestMockAssignmentSectionRepository(),
                                        judgeServiceClient = TestMockJudgeServiceClient()
                                )

                        checkAll(100, Arb.int(1..100)) { total ->
                                val score = useCase.calculateScore(total, total)
                                score shouldBe 100
                        }
                }

                test("全テストケース失敗時は0点") {
                        val useCase =
                                SubmissionUseCase(
                                        submissionRepository = TestMockSubmissionRepository(),
                                        assignmentSectionRepository =
                                                TestMockAssignmentSectionRepository(),
                                        judgeServiceClient = TestMockJudgeServiceClient()
                                )

                        checkAll(100, Arb.int(1..100)) { total ->
                                val score = useCase.calculateScore(0, total)
                                score shouldBe 0
                        }
                }

                test("テストケースが0件の場合は0点") {
                        val useCase =
                                SubmissionUseCase(
                                        submissionRepository = TestMockSubmissionRepository(),
                                        assignmentSectionRepository =
                                                TestMockAssignmentSectionRepository(),
                                        judgeServiceClient = TestMockJudgeServiceClient()
                                )

                        val score = useCase.calculateScore(0, 0)
                        score shouldBe 0
                }

                test("部分点は整数除算で計算される（切り捨て）") {
                        val useCase =
                                SubmissionUseCase(
                                        submissionRepository = TestMockSubmissionRepository(),
                                        assignmentSectionRepository =
                                                TestMockAssignmentSectionRepository(),
                                        judgeServiceClient = TestMockJudgeServiceClient()
                                )

                        useCase.calculateScore(1, 3) shouldBe 33
                        useCase.calculateScore(2, 3) shouldBe 66
                        useCase.calculateScore(1, 7) shouldBe 14
                }
        })

/** Feature: assignment-execution-system, Property 4: 再提出時の新規レコード作成 Validates: Requirements 3.4 */
class ResubmissionPropertyTest :
        FunSpec({
                test("再提出時は新しいSubmissionが作成される（既存は変更されない）") {
                        val mockRepository = TestTrackingSubmissionRepository()
                        val mockSectionRepository =
                                TestMockAssignmentSectionRepositoryWithAssignment()
                        val mockJudgeClient = TestMockJudgeServiceClient()
                        val useCase =
                                SubmissionUseCase(
                                        mockRepository,
                                        mockSectionRepository,
                                        mockJudgeClient
                                )

                        checkAll(100, Arb.int(2..10)) { submissionCount ->
                                mockRepository.clear()
                                val userId = java.util.UUID.randomUUID()

                                repeat(submissionCount) {
                                        useCase.submitCode(
                                                userId = userId,
                                                assignmentSubjectId = 1L,
                                                sectionId = 1,
                                                code = "int main() { return $it; }",
                                                language = Language.C
                                        )
                                }

                                mockRepository.savedCount shouldBe submissionCount
                        }
                }
        })
