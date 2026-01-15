package com.j15.backend.application.usecase.assignment

import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.assignment.TestResult
import com.j15.backend.domain.model.assignment.Verdict
import com.j15.backend.domain.repository.TestResultRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** テスト結果管理ユースケース */
@Service
@Transactional
class TestResultUseCase(private val testResultRepository: TestResultRepository) {

    /**
     * テスト結果を保存
     * @param submissionId 提出ID
     * @param results 判定結果リスト
     * @return 保存されたテスト結果リスト
     */
    fun saveResults(submissionId: Long, results: List<JudgeResult>): List<TestResult> {
        val testResults =
                results.mapIndexed { index, result ->
                    TestResult(
                            id = null, // 自動採番
                            submissionId = SubmissionId(submissionId),
                            testCaseIndex = index,
                            verdict = result.verdict,
                            executionTime = result.executionTime,
                            memoryUsed = result.memoryUsed,
                            visible = result.visible,
                            actualOutput = if (result.visible) result.actualOutput else null,
                            errorMessage = result.errorMessage
                    )
                }

        return testResultRepository.saveAll(testResults)
    }

    /**
     * 提出のテスト結果を取得
     * @param submissionId 提出ID
     * @return テスト結果リスト（インデックス順）
     */
    @Transactional(readOnly = true)
    fun getResultsBySubmission(submissionId: Long): List<TestResult> {
        return testResultRepository.findBySubmissionId(SubmissionId(submissionId))
    }

    /**
     * 可視性フィルタリング済みのテスト結果を取得 visible=falseのテスト結果はactualOutputを隠す
     * @param submissionId 提出ID
     * @return フィルタリング済みテスト結果リスト
     */
    @Transactional(readOnly = true)
    fun getFilteredResultsBySubmission(submissionId: Long): List<TestResult> {
        return testResultRepository.findBySubmissionId(SubmissionId(submissionId)).map { result ->
            if (result.visible) {
                result
            } else {
                // 非可視テストケースはactualOutputを隠す
                result.copy(actualOutput = null)
            }
        }
    }
}

/** Judge Serviceからの判定結果 */
data class JudgeResult(
        val verdict: Verdict,
        val executionTime: Int? = null,
        val memoryUsed: Int? = null,
        val visible: Boolean,
        val actualOutput: String? = null,
        val errorMessage: String? = null
)
