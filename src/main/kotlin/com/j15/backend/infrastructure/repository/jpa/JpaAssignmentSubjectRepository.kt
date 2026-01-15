package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.AssignmentSubjectJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaAssignmentSubjectRepository : JpaRepository<AssignmentSubjectJpaEntity, Long> {
    fun findAllByOrderByIdAsc(): List<AssignmentSubjectJpaEntity>
}
