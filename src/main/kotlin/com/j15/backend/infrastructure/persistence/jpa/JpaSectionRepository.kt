package com.j15.backend.infrastructure.persistence.jpa

import com.j15.backend.infrastructure.persistence.entity.SectionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface JpaSectionRepository : JpaRepository<SectionEntity, Int> {

    /** 指定題材のセクション一覧を取得 */
    fun findBySubjectId(subjectId: Long): List<SectionEntity>

    /** 指定題材のセクション数をカウント */
    fun countBySubjectId(subjectId: Long): Long

    /** 指定題材の特定セクションを検索 */
    @Query(
            "SELECT s FROM SectionEntity s WHERE s.subjectId = :subjectId AND s.sectionId = :sectionId"
    )
    fun findBySubjectIdAndSectionId(subjectId: Long, sectionId: Int): SectionEntity?
}
