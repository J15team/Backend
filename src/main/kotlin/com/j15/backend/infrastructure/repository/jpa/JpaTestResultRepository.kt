package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.TestResultJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaTestResultRepository : JpaRepository<TestResultJpaEntity, Long> {

    fun findBySubmissionIdOrderByTestCaseIndexAsc(submissionId: Long): List<TestResultJpaEntity>
}
