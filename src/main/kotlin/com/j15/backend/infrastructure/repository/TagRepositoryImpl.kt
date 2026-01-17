package com.j15.backend.infrastructure.repository

import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.model.tag.TagType
import com.j15.backend.domain.repository.tag.TagRepository
import com.j15.backend.infrastructure.converter.TagConverter
import com.j15.backend.infrastructure.repository.jpa.JpaTagRepository
import org.springframework.stereotype.Repository

@Repository
class TagRepositoryImpl(
        private val jpaTagRepository: JpaTagRepository,
        private val tagConverter: TagConverter
) : TagRepository {

    override fun findById(tagId: TagId): Tag? {
        return jpaTagRepository.findById(tagId.value).map { tagConverter.toDomain(it) }.orElse(null)
    }

    override fun findByName(name: TagName): Tag? {
        return jpaTagRepository.findByName(name.value)?.let { tagConverter.toDomain(it) }
    }

    override fun findAll(): List<Tag> {
        return jpaTagRepository.findAllByOrderByNameAsc().map { tagConverter.toDomain(it) }
    }

    override fun findByType(type: TagType): List<Tag> {
        return jpaTagRepository.findByType(type.name).map { tagConverter.toDomain(it) }
    }

    override fun searchByName(query: String): List<Tag> {
        return jpaTagRepository.findByNameContainingIgnoreCase(query).map {
            tagConverter.toDomain(it)
        }
    }

    override fun save(tag: Tag): Tag {
        val entity =
                if (tag.id.value == 0L) {
                    tagConverter.toNewEntity(tag)
                } else {
                    tagConverter.toEntity(tag)
                }
        val saved = jpaTagRepository.save(entity)
        return tagConverter.toDomain(saved)
    }

    override fun deleteById(tagId: TagId) {
        jpaTagRepository.deleteById(tagId.value)
    }

    override fun existsById(tagId: TagId): Boolean {
        return jpaTagRepository.existsById(tagId.value)
    }

    override fun existsByName(name: TagName): Boolean {
        return jpaTagRepository.existsByName(name.value)
    }
}
