package com.j15.backend.domain.repository

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId

// セクションリポジトリ（ドメイン層のインターフェース）
interface SectionRepository {
    fun findById(sectionId: SectionId): Section?
    fun findAll(): List<Section>
    fun save(section: Section): Section
    fun existsById(sectionId: SectionId): Boolean
    fun count(): Int // セクション総数を取得
}
