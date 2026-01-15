package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.assignment.TestResult
import com.j15.backend.domain.model.assignment.TestResultId
import com.j15.backend.domain.model.assignment.Verdict
import com.j15.backend.infrastructure.entity.TestResultJpaEntity
import org.springframework.stereotype.Component

@Component
class TestResultConverter {

    fun toDomain(entity: TestResultJpaEntity): TestResult {
        return TestResult(
                id = TestResultId(entity.id),
                submissionId = SubmissionId(entity.submissionId),
                testCaseIndex = entity.testCaseIndex,
                verdict = Verdict.valueOf(entity.verdict),
                executionTime = entity.executionTime,
                memoryUsed = entity.memoryUsed,
                visible = entity.visible,
                actualOutput = entity.actualOutput,
                errorMessage = entity.errorMessage
        )
    }

    fun toEntity(domain: TestResult): TestResultJpaEntity {
        return TestResultJpaEntity(
                id = domain.id?.value ?: 0L,
                submissionId = domain.submissionId.value,
                testCaseIndex = domain.testCaseIndex,
                verdict = domain.verdict.name,
                executionTime = domain.executionTime,
                memoryUsed = domain.memoryUsed,
                visible = domain.visible,
                actualOutput = domain.actualOutput,
                errorMessage = domain.errorMessage
        )
    }
}
