package com.j15.backend.domain.repository

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId

// セクションリポジトリ（ドメイン層のインターフェース）
interface SectionRepository {
    fun findById(subjectId: SubjectId, sectionId: SectionId): Section?
    fun findAllBySubjectId(subjectId: SubjectId): List<Section>
    fun save(section: Section): Section
    fun existsById(subjectId: SubjectId, sectionId: SectionId): Boolean
    fun countBySubjectId(subjectId: SubjectId): Int // 題材ごとのセクション総数
}
