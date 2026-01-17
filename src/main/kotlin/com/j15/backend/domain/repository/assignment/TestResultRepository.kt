package com.j15.backend.domain.repository.assignment

import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.assignment.TestResult

/** テスト結果リポジトリ（ドメイン層のインターフェース） */
interface TestResultRepository {
    fun findBySubmissionId(submissionId: SubmissionId): List<TestResult>
    fun save(testResult: TestResult): TestResult
    fun saveAll(testResults: List<TestResult>): List<TestResult>
}
