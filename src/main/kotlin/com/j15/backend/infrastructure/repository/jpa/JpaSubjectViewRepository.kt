package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.SubjectViewId
import com.j15.backend.infrastructure.entity.SubjectViewJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaSubjectViewRepository : JpaRepository<SubjectViewJpaEntity, SubjectViewId> {

    fun countBySubjectId(subjectId: Long): Long

    fun existsBySubjectIdAndUserId(subjectId: Long, userId: UUID): Boolean

    @Query("""
        SELECT sv.subjectId, COUNT(sv.subjectId) as viewCount
        FROM SubjectViewJpaEntity sv
        GROUP BY sv.subjectId
        ORDER BY viewCount DESC
        LIMIT :limit
    """)
    fun findTopSubjectIdsByViewCount(limit: Int): List<Array<Any>>
}
