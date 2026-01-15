package com.j15.backend.domain.model.assignment

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: assignment-execution-system, Property 1: セクション課題設定の整合性 Validates: Requirements 1.1, 1.3,
 * 1.4
 *
 * For any AssignmentSection, hasAssignment=true の場合は testCases が存在し、 hasAssignment=false の場合は
 * testCases が null である
 */
class AssignmentSectionPropertyTest :
        FunSpec({
            val validSubjectIdArb: Arb<Long> = Arb.long(1L..1000L)
            val validSectionIdArb: Arb<Int> = Arb.int(0..100)
            val validTitleArb: Arb<String> = Arb.string(1..50).map { "Title_$it" }
            val validTimeLimitArb: Arb<Int> = Arb.int(100..10000)
            val validMemoryLimitArb: Arb<Int> = Arb.int(16..1024)

            val validTestCasesJson =
                    """[{"input":"","expected":"Hello, World!\n","visible":true}]"""

            test("hasAssignment=true の場合、testCases, timeLimit, memoryLimit が必須") {
                checkAll(
                        100,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validTitleArb,
                        validTimeLimitArb,
                        validMemoryLimitArb
                ) { subjectId, sectionId, title, timeLimit, memoryLimit ->
                    val section =
                            AssignmentSection(
                                    assignmentSubjectId = AssignmentSubjectId(subjectId),
                                    sectionId = AssignmentSectionId(sectionId),
                                    title = title,
                                    hasAssignment = true,
                                    testCases = validTestCasesJson,
                                    timeLimit = timeLimit,
                                    memoryLimit = memoryLimit
                            )

                    section.hasAssignment shouldBe true
                    section.testCases shouldNotBe null
                    section.timeLimit shouldNotBe null
                    section.memoryLimit shouldNotBe null
                }
            }

            test("hasAssignment=false の場合、testCases は null である") {
                checkAll(100, validSubjectIdArb, validSectionIdArb, validTitleArb) {
                        subjectId,
                        sectionId,
                        title ->
                    val section =
                            AssignmentSection(
                                    assignmentSubjectId = AssignmentSubjectId(subjectId),
                                    sectionId = AssignmentSectionId(sectionId),
                                    title = title,
                                    hasAssignment = false,
                                    testCases = null
                            )

                    section.hasAssignment shouldBe false
                    section.testCases shouldBe null
                }
            }

            test("hasAssignment=true で testCases が null の場合、例外が発生する") {
                checkAll(
                        100,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validTitleArb,
                        validTimeLimitArb,
                        validMemoryLimitArb
                ) { subjectId, sectionId, title, timeLimit, memoryLimit ->
                    shouldThrow<IllegalArgumentException> {
                        AssignmentSection(
                                assignmentSubjectId = AssignmentSubjectId(subjectId),
                                sectionId = AssignmentSectionId(sectionId),
                                title = title,
                                hasAssignment = true,
                                testCases = null,
                                timeLimit = timeLimit,
                                memoryLimit = memoryLimit
                        )
                    }
                }
            }

            test("hasAssignment=false で testCases が設定されている場合、例外が発生する") {
                checkAll(100, validSubjectIdArb, validSectionIdArb, validTitleArb) {
                        subjectId,
                        sectionId,
                        title ->
                    shouldThrow<IllegalArgumentException> {
                        AssignmentSection(
                                assignmentSubjectId = AssignmentSubjectId(subjectId),
                                sectionId = AssignmentSectionId(sectionId),
                                title = title,
                                hasAssignment = false,
                                testCases = validTestCasesJson
                        )
                    }
                }
            }

            test("hasAssignment=true で timeLimit が null の場合、例外が発生する") {
                checkAll(
                        100,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validTitleArb,
                        validMemoryLimitArb
                ) { subjectId, sectionId, title, memoryLimit ->
                    shouldThrow<IllegalArgumentException> {
                        AssignmentSection(
                                assignmentSubjectId = AssignmentSubjectId(subjectId),
                                sectionId = AssignmentSectionId(sectionId),
                                title = title,
                                hasAssignment = true,
                                testCases = validTestCasesJson,
                                timeLimit = null,
                                memoryLimit = memoryLimit
                        )
                    }
                }
            }

            test("hasAssignment=true で memoryLimit が null の場合、例外が発生する") {
                checkAll(
                        100,
                        validSubjectIdArb,
                        validSectionIdArb,
                        validTitleArb,
                        validTimeLimitArb
                ) { subjectId, sectionId, title, timeLimit ->
                    shouldThrow<IllegalArgumentException> {
                        AssignmentSection(
                                assignmentSubjectId = AssignmentSubjectId(subjectId),
                                sectionId = AssignmentSectionId(sectionId),
                                title = title,
                                hasAssignment = true,
                                testCases = validTestCasesJson,
                                timeLimit = timeLimit,
                                memoryLimit = null
                        )
                    }
                }
            }
        })
