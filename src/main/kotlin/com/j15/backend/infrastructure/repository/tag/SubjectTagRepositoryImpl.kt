package com.j15.backend.infrastructure.repository.tag

import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.repository.tag.SubjectTagRepository
import com.j15.backend.infrastructure.converter.TagConverter
import com.j15.backend.infrastructure.entity.SubjectTagId
import com.j15.backend.infrastructure.entity.SubjectTagJpaEntity
import com.j15.backend.infrastructure.repository.jpa.JpaSubjectTagRepository
import com.j15.backend.infrastructure.repository.jpa.JpaTagRepository
import java.time.LocalDateTime
import org.springframework.stereotype.Repository

@Repository
class SubjectTagRepositoryImpl(
        private val jpaSubjectTagRepository: JpaSubjectTagRepository,
        private val jpaTagRepository: JpaTagRepository,
        private val tagConverter: TagConverter
) : SubjectTagRepository {

    override fun findTagsBySubjectId(subjectId: SubjectId): List<Tag> {
        val associations = jpaSubjectTagRepository.findBySubjectId(subjectId.value)
        val tagIds = associations.map { it.tagId }
        return jpaTagRepository.findAllById(tagIds).map { tagConverter.toDomain(it) }
    }

    override fun findSubjectIdsByTagId(tagId: TagId): List<SubjectId> {
        return jpaSubjectTagRepository.findByTagId(tagId.value).map { SubjectId(it.subjectId) }
    }

    override fun findSubjectIdsByTagIds(tagIds: List<TagId>): List<SubjectId> {
        if (tagIds.isEmpty()) return emptyList()
        val ids = tagIds.map { it.value }
        return jpaSubjectTagRepository.findSubjectIdsHavingAnyTags(ids).map { SubjectId(it) }
    }

    override fun findAllAssociations(): Map<TagId, List<SubjectId>> {
        return jpaSubjectTagRepository
                .findAll()
                .groupBy({ TagId(it.tagId) }, { SubjectId(it.subjectId) })
    }

    override fun addTagToSubject(subjectId: SubjectId, tagId: TagId) {
        if (!existsAssociation(subjectId, tagId)) {
            val entity =
                    SubjectTagJpaEntity(
                            subjectId = subjectId.value,
                            tagId = tagId.value,
                            createdAt = LocalDateTime.now()
                    )
            jpaSubjectTagRepository.save(entity)
        }
    }

    override fun removeTagFromSubject(subjectId: SubjectId, tagId: TagId) {
        val id = SubjectTagId(subjectId.value, tagId.value)
        if (jpaSubjectTagRepository.existsById(id)) {
            jpaSubjectTagRepository.deleteById(id)
        }
    }

    override fun removeAllTagsFromSubject(subjectId: SubjectId) {
        jpaSubjectTagRepository.deleteBySubjectId(subjectId.value)
    }

    override fun removeTagFromAllSubjects(tagId: TagId) {
        jpaSubjectTagRepository.deleteByTagId(tagId.value)
    }

    override fun existsAssociation(subjectId: SubjectId, tagId: TagId): Boolean {
        return jpaSubjectTagRepository.existsBySubjectIdAndTagId(subjectId.value, tagId.value)
    }
}
