package com.j15.backend.domain.repository.tag

import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.model.tag.TagType

/** タグリポジトリ（ドメイン層のインターフェース） */
interface TagRepository {
    fun findById(tagId: TagId): Tag?
    fun findByName(name: TagName): Tag?
    fun findAll(): List<Tag>
    fun findByType(type: TagType): List<Tag>
    fun searchByName(query: String): List<Tag>
    fun save(tag: Tag): Tag
    fun deleteById(tagId: TagId)
    fun existsById(tagId: TagId): Boolean
    fun existsByName(name: TagName): Boolean
}
