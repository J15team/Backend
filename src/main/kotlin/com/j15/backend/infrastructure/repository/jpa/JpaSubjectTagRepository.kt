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

    /** 指定したタグのいずれかを持つ題材IDを取得（OR条件） */
    @Query(
            """
        SELECT DISTINCT st.subjectId FROM SubjectTagJpaEntity st 
        WHERE st.tagId IN :tagIds
    """
    )
    fun findSubjectIdsHavingAnyTags(@Param("tagIds") tagIds: List<Long>): List<Long>
}
