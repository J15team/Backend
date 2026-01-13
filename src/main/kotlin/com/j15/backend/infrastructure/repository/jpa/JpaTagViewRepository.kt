package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.TagViewId
import com.j15.backend.infrastructure.entity.TagViewJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface JpaTagViewRepository : JpaRepository<TagViewJpaEntity, TagViewId> {

    fun countByTagId(tagId: Long): Long

    fun existsByTagIdAndUserId(tagId: Long, userId: UUID): Boolean

    @Query("""
        SELECT tv.tagId, COUNT(tv.tagId) as viewCount
        FROM TagViewJpaEntity tv
        GROUP BY tv.tagId
        ORDER BY viewCount DESC
        LIMIT :limit
    """)
    fun findTopTagIdsByViewCount(limit: Int): List<Array<Any>>
}
