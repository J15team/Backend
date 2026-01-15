package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.assignment.TestResult
import com.j15.backend.domain.model.assignment.TestResultId
import com.j15.backend.domain.model.assignment.Verdict
import com.j15.backend.domain.repository.TestResultRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

/**
 * Feature: assignment-execution-system, Property 7: テストケース可視性の制御 Validates: Requirements 5.5, 5.6
 *
 * For any テスト結果, visible=true のケースは入出力詳細を含み、 visible=false のケースは結果のみ（入出力非公開）
 */
class TestResultUseCasePropertyTest :
        FunSpec({
            test("visible=true のテスト結果は actualOutput を含む") {
                val mockRepository = InMemoryTestResultRepository()
                val useCase = TestResultUseCase(mockRepository)

                checkAll(100, Arb.long(1L..1000L), Arb.string(1..50)) { submissionId, output ->
                    mockRepository.clear()
                    mockRepository.addResult(
                            TestResult(
                                    id = TestResultId(1L),
                                    submissionId = SubmissionId(submissionId),
                                    testCaseIndex = 0,
                                    verdict = Verdict.AC,
                                    executionTime = 100,
                                    memoryUsed = 1024,
                                    visible = true,
                                    actualOutput = output
                            )
                    )

                    val results = useCase.getFilteredResultsBySubmission(submissionId)

                    results.size shouldBe 1
                    results[0].visible shouldBe true
                    results[0].actualOutput shouldBe output
                }
            }

            test("visible=false のテスト結果は actualOutput が null になる") {
                val mockRepository = InMemoryTestResultRepository()
                val useCase = TestResultUseCase(mockRepository)

                checkAll(100, Arb.long(1L..1000L), Arb.string(1..50)) { submissionId, output ->
                    mockRepository.clear()
                    mockRepository.addResult(
                            TestResult(
                                    id = TestResultId(1L),
                                    submissionId = SubmissionId(submissionId),
                                    testCaseIndex = 0,
                                    verdict = Verdict.AC,
                                    executionTime = 100,
                                    memoryUsed = 1024,
                                    visible = false,
                                    actualOutput = output // 元々は値がある
                            )
                    )

                    val results = useCase.getFilteredResultsBySubmission(submissionId)

                    results.size shouldBe 1
                    results[0].visible shouldBe false
                    results[0].actualOutput shouldBe null // フィルタリングで隠される
                }
            }

            test("可視・非可視テストケースが混在する場合、それぞれ適切にフィルタリングされる") {
                val mockRepository = InMemoryTestResultRepository()
                val useCase = TestResultUseCase(mockRepository)

                checkAll(100, Arb.int(1..10)) { visibleCount ->
                    mockRepository.clear()
                    val submissionId = 1L

                    // 可視テストケース
                    repeat(visibleCount) { i ->
                        mockRepository.addResult(
                                TestResult(
                                        id = TestResultId(i.toLong() + 1),
                                        submissionId = SubmissionId(submissionId),
                                        testCaseIndex = i,
                                        verdict = Verdict.AC,
                                        visible = true,
                                        actualOutput = "output_$i"
                                )
                        )
                    }

                    // 非可視テストケース
                    repeat(visibleCount) { i ->
                        mockRepository.addResult(
                                TestResult(
                                        id = TestResultId((visibleCount + i).toLong() + 1),
                                        submissionId = SubmissionId(submissionId),
                                        testCaseIndex = visibleCount + i,
                                        verdict = Verdict.AC,
                                        visible = false,
                                        actualOutput = "hidden_output_$i"
                                )
                        )
                    }

                    val results = useCase.getFilteredResultsBySubmission(submissionId)

                    results.size shouldBe visibleCount * 2

                    // 可視テストケースはactualOutputを持つ
                    results.filter { it.visible }.forEach { result ->
                        result.actualOutput shouldNotBe null
                    }

                    // 非可視テストケースはactualOutputがnull
                    results.filter { !it.visible }.forEach { result ->
                        result.actualOutput shouldBe null
                    }
                }
            }

            test("verdict は可視性に関係なく常に返される") {
                val mockRepository = InMemoryTestResultRepository()
                val useCase = TestResultUseCase(mockRepository)

                val verdicts = Verdict.entries

                checkAll(100, Arb.boolean()) { visible ->
                    verdicts.forEachIndexed { index, verdict ->
                        mockRepository.clear()
                        mockRepository.addResult(
                                TestResult(
                                        id = TestResultId(1L),
                                        submissionId = SubmissionId(1L),
                                        testCaseIndex = index,
                                        verdict = verdict,
                                        visible = visible,
                                        actualOutput = "output"
                                )
                        )

                        val results = useCase.getFilteredResultsBySubmission(1L)

                        results.size shouldBe 1
                        results[0].verdict shouldBe verdict
                    }
                }
            }
        })

// テスト用インメモリリポジトリ
private class InMemoryTestResultRepository : TestResultRepository {
    private val results = mutableListOf<TestResult>()

    fun addResult(result: TestResult) {
        results.add(result)
    }

    fun clear() {
        results.clear()
    }

    override fun findBySubmissionId(submissionId: SubmissionId): List<TestResult> {
        return results.filter { it.submissionId == submissionId }.sortedBy { it.testCaseIndex }
    }

    override fun save(testResult: TestResult): TestResult {
        results.add(testResult)
        return testResult
    }

    override fun saveAll(testResults: List<TestResult>): List<TestResult> {
        results.addAll(testResults)
        return testResults
    }
}
