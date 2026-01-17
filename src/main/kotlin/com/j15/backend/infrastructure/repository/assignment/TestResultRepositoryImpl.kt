package com.j15.backend.infrastructure.repository.assignment

import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.assignment.TestResult
import com.j15.backend.domain.repository.assignment.TestResultRepository
import com.j15.backend.infrastructure.converter.TestResultConverter
import com.j15.backend.infrastructure.repository.jpa.JpaTestResultRepository
import org.springframework.stereotype.Repository

@Repository
class TestResultRepositoryImpl(
        private val jpaRepository: JpaTestResultRepository,
        private val converter: TestResultConverter
) : TestResultRepository {

    override fun findBySubmissionId(submissionId: SubmissionId): List<TestResult> {
        return jpaRepository.findBySubmissionIdOrderByTestCaseIndexAsc(submissionId.value).map {
            converter.toDomain(it)
        }
    }

    override fun save(testResult: TestResult): TestResult {
        val entity = converter.toEntity(testResult)
        val saved = jpaRepository.save(entity)
        return converter.toDomain(saved)
    }

    override fun saveAll(testResults: List<TestResult>): List<TestResult> {
        val entities = testResults.map { converter.toEntity(it) }
        val saved = jpaRepository.saveAll(entities)
        return saved.map { converter.toDomain(it) }
    }
}
