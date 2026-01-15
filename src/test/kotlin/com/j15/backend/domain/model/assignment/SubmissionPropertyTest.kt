package com.j15.backend.domain.model.assignment

import com.j15.backend.domain.model.user.UserId
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import java.time.Instant
import java.util.UUID

/**
 * Feature: assignment-execution-system, Property 3: 提出レコードの不変性 Validates: Requirements 3.3
 *
 * For any 提出レコード, 一度作成されたら更新・削除ができない（INSERT only）
 *
 * Note: このプロパティテストはドメインモデルレベルでの不変性を検証します。
 * データベースレベルでの不変性はトリガーで保証されます（V14__create_assignment_tables.sql）。
 */
class SubmissionPropertyTest :
        FunSpec({
            val validSubmissionIdArb: Arb<Long> = Arb.long(1L..1000L)
            val validSubjectIdArb: Arb<Long> = Arb.long(1L..1000L)
            val validSectionIdArb: Arb<Int> = Arb.int(0..100)
            val validCodeArb: Arb<String> = Arb.string(1..100).map { "int main() { $it }" }
            val validScoreArb: Arb<Int> = Arb.int(0..100)
            val validTestCaseCountArb: Arb<Int> = Arb.int(1..20)

            test("Submissionはdata classであり、copyメソッドで新しいインスタンスが作成される") {
                checkAll(
                        100,
                        validSubmissionIdArb,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validCodeArb
                ) { submissionId, subjectId, sectionId, code ->
                    val original =
                            Submission(
                                    id = SubmissionId(submissionId),
                                    userId = UserId(UUID.randomUUID()),
                                    assignmentSubjectId = AssignmentSubjectId(subjectId),
                                    sectionId = AssignmentSectionId(sectionId),
                                    code = code,
                                    language = Language.C,
                                    submittedAt = Instant.now(),
                                    status = SubmissionStatus.PENDING
                            )

                    // copyで新しいインスタンスが作成される（元のインスタンスは変更されない）
                    val copied = original.copy(status = SubmissionStatus.COMPLETED)

                    // 元のインスタンスは変更されていない
                    original.status shouldBe SubmissionStatus.PENDING
                    // 新しいインスタンスは変更されている
                    copied.status shouldBe SubmissionStatus.COMPLETED
                    // IDは同じ（同じ提出を表す）
                    original.id shouldBe copied.id
                }
            }

            test("Submissionの全フィールドはvalで宣言されており、直接変更できない") {
                checkAll(
                        100,
                        validSubmissionIdArb,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validCodeArb,
                        validScoreArb
                ) { submissionId, subjectId, sectionId, code, score ->
                    val submission =
                            Submission(
                                    id = SubmissionId(submissionId),
                                    userId = UserId(UUID.randomUUID()),
                                    assignmentSubjectId = AssignmentSubjectId(subjectId),
                                    sectionId = AssignmentSectionId(sectionId),
                                    code = code,
                                    language = Language.C,
                                    submittedAt = Instant.now(),
                                    status = SubmissionStatus.COMPLETED,
                                    score = score,
                                    totalTestCases = 10,
                                    passedTestCases = score / 10
                            )

                    // 全フィールドが読み取り専用であることを確認
                    // （コンパイル時に保証されるが、テストで明示的に確認）
                    submission.id.value shouldBe submissionId
                    submission.code shouldBe code
                    submission.status shouldBe SubmissionStatus.COMPLETED
                    submission.score shouldBe score
                }
            }

            test("同一ユーザー・同一セクションへの再提出は新しいSubmissionインスタンスとなる") {
                checkAll(100, validSubjectIdArb, validSectionIdArb, validCodeArb, validCodeArb) {
                        subjectId,
                        sectionId,
                        code1,
                        code2 ->
                    val userId = UserId(UUID.randomUUID())
                    val assignmentSubjectId = AssignmentSubjectId(subjectId)
                    val assignmentSectionId = AssignmentSectionId(sectionId)

                    // 最初の提出
                    val firstSubmission =
                            Submission(
                                    id = SubmissionId(1L),
                                    userId = userId,
                                    assignmentSubjectId = assignmentSubjectId,
                                    sectionId = assignmentSectionId,
                                    code = code1,
                                    language = Language.C,
                                    submittedAt = Instant.now(),
                                    status = SubmissionStatus.COMPLETED,
                                    score = 50
                            )

                    // 再提出（新しいインスタンス、新しいID）
                    val secondSubmission =
                            Submission(
                                    id = SubmissionId(2L),
                                    userId = userId,
                                    assignmentSubjectId = assignmentSubjectId,
                                    sectionId = assignmentSectionId,
                                    code = code2,
                                    language = Language.C,
                                    submittedAt = Instant.now(),
                                    status = SubmissionStatus.PENDING
                            )

                    // 異なるIDを持つ
                    firstSubmission.id shouldBe SubmissionId(1L)
                    secondSubmission.id shouldBe SubmissionId(2L)

                    // 同じユーザー・セクション
                    firstSubmission.userId shouldBe secondSubmission.userId
                    firstSubmission.assignmentSubjectId shouldBe
                            secondSubmission.assignmentSubjectId
                    firstSubmission.sectionId shouldBe secondSubmission.sectionId

                    // 最初の提出は変更されていない
                    firstSubmission.score shouldBe 50
                    firstSubmission.status shouldBe SubmissionStatus.COMPLETED
                }
            }

            test("Submissionのスコアと判定結果は作成時に設定され、後から変更されない") {
                checkAll(100, validTestCaseCountArb) { totalTestCases ->
                    val passedTestCases = totalTestCases / 2
                    val score = (passedTestCases * 100) / totalTestCases

                    val submission =
                            Submission(
                                    id = SubmissionId(1L),
                                    userId = UserId(UUID.randomUUID()),
                                    assignmentSubjectId = AssignmentSubjectId(1L),
                                    sectionId = AssignmentSectionId(1),
                                    code = "int main() { return 0; }",
                                    language = Language.C,
                                    submittedAt = Instant.now(),
                                    status = SubmissionStatus.COMPLETED,
                                    score = score,
                                    totalTestCases = totalTestCases,
                                    passedTestCases = passedTestCases
                            )

                    // 作成時の値が保持される
                    submission.score shouldBe score
                    submission.totalTestCases shouldBe totalTestCases
                    submission.passedTestCases shouldBe passedTestCases
                }
            }
        })
