package com.j15.backend.infrastructure.repository.jpa

import com.j15.backend.infrastructure.entity.SubjectTagId
import com.j15.backend.infrastructure.entity.SubjectTagJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface JpaSubjectTagRepository : JpaRepository<SubjectTagJpaEntity, SubjectTagId> {
    fun findBySubjectId(subjectId: Long): List<SubjectTagJpaEntity>
    fun findByTagId(tagId: Long): List<SubjectTagJpaEntity>
    fun deleteBySubjectId(subjectId: Long)
    fun deleteByTagId(tagId: Long)
    fun existsBySubjectIdAndTagId(subjectId: Long, tagId: Long): Boolean

    @Query(
            """
        SELECT st.subjectId FROM SubjectTagJpaEntity st 
        WHERE st.tagId IN :tagIds 
        GROUP BY st.subjectId 
        HAVING COUNT(DISTINCT st.tagId) = :tagCount
    """
    )
    fun findSubjectIdsHavingAllTags(
            @Param("tagIds") tagIds: List<Long>,
            @Param("tagCount") tagCount: Long
    ): List<Long>
}
