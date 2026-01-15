package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.*
import com.j15.backend.domain.model.user.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.time.Instant
import java.util.UUID

/** Feature: assignment-execution-system, Property 8: 提出履歴の時系列順序 Validates: Requirements 6.1 */
class SubmissionHistoryPropertyTest :
        FunSpec({
            test("提出履歴は提出日時の降順で返される") {
                checkAll(100, Arb.int(2..20)) { submissionCount ->
                    val mockRepository = TestInMemorySubmissionRepository()
                    val mockSectionRepository = TestMockAssignmentSectionRepository()
                    val mockJudgeClient = TestMockJudgeServiceClient()
                    val useCase =
                            SubmissionUseCase(
                                    mockRepository,
                                    mockSectionRepository,
                                    mockJudgeClient
                            )

                    val userId = UUID.randomUUID()
                    val subjectId = 1L
                    val sectionId = 1

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

                    submissions.shuffled().forEach { mockRepository.addSubmission(it) }

                    val history = useCase.getSubmissionHistory(userId, subjectId, sectionId)

                    history.size shouldBe submissionCount
                    for (i in 0 until history.size - 1) {
                        val current = history[i].submittedAt
                        val next = history[i + 1].submittedAt
                        (current >= next) shouldBe true
                    }
                }
            }

            test("同一時刻の提出がある場合でも安定してソートされる") {
                val mockRepository = TestInMemorySubmissionRepository()
                val mockSectionRepository = TestMockAssignmentSectionRepository()
                val mockJudgeClient = TestMockJudgeServiceClient()
                val useCase =
                        SubmissionUseCase(mockRepository, mockSectionRepository, mockJudgeClient)

                val userId = UUID.randomUUID()
                val subjectId = 1L
                val sectionId = 1
                val sameTime = Instant.now()

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

                history.size shouldBe 5
                history.all { it.submittedAt == sameTime } shouldBe true
            }

            test("空の履歴は空リストを返す") {
                val mockRepository = TestInMemorySubmissionRepository()
                val mockSectionRepository = TestMockAssignmentSectionRepository()
                val mockJudgeClient = TestMockJudgeServiceClient()
                val useCase =
                        SubmissionUseCase(mockRepository, mockSectionRepository, mockJudgeClient)

                val history = useCase.getSubmissionHistory(UUID.randomUUID(), 1L, 1)

                history.size shouldBe 0
            }
        })
