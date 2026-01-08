package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.SubjectJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaSubjectRepository : JpaRepository<SubjectJpaEntity, Long> {
    /** 題材IDの昇順で全題材を取得 */
    fun findAllByOrderBySubjectIdAsc(): List<SubjectJpaEntity>
}
