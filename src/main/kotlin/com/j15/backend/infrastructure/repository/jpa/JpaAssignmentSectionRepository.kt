package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.AssignmentSectionEntityId
import com.j15.backend.infrastructure.entity.AssignmentSectionJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaAssignmentSectionRepository :
        JpaRepository<AssignmentSectionJpaEntity, AssignmentSectionEntityId> {
        fun findByAssignmentSubjectIdOrderBySectionIdAsc(
                assignmentSubjectId: Long
        ): List<AssignmentSectionJpaEntity>

        fun deleteByAssignmentSubjectId(assignmentSubjectId: Long)
}
