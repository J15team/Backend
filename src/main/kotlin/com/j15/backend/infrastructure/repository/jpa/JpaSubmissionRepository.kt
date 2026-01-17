package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.SubmissionJpaEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaSubmissionRepository : JpaRepository<SubmissionJpaEntity, Long> {

        fun findByUserIdAndAssignmentSubjectIdAndSectionIdOrderBySubmittedAtDesc(
                userId: UUID,
                assignmentSubjectId: Long,
                sectionId: Int
        ): List<SubmissionJpaEntity>

        fun findByUserIdAndAssignmentSubjectIdOrderBySubmittedAtDesc(
                userId: UUID,
                assignmentSubjectId: Long
        ): List<SubmissionJpaEntity>

        fun findByAssignmentSubjectIdAndSectionIdOrderBySubmittedAtDesc(
                assignmentSubjectId: Long,
                sectionId: Int
        ): List<SubmissionJpaEntity>

        fun findByAssignmentSubjectIdOrderBySubmittedAtDesc(
                assignmentSubjectId: Long
        ): List<SubmissionJpaEntity>
}
