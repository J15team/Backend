package com.j15.backend.infrastructure.repository

import com.j15.backend.domain.model.assignment.AssignmentSection
import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.repository.AssignmentSectionRepository
import com.j15.backend.infrastructure.converter.AssignmentSectionConverter
import com.j15.backend.infrastructure.entity.AssignmentSectionEntityId
import com.j15.backend.infrastructure.repository.jpa.JpaAssignmentSectionRepository
import org.springframework.stereotype.Repository

@Repository
class AssignmentSectionRepositoryImpl(
        private val jpaRepository: JpaAssignmentSectionRepository,
        private val converter: AssignmentSectionConverter
) : AssignmentSectionRepository {

    override fun findById(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): AssignmentSection? {
        val entityId = AssignmentSectionEntityId(subjectId.value, sectionId.value)
        return jpaRepository.findById(entityId).map { converter.toDomain(it) }.orElse(null)
    }

    override fun findBySubjectId(subjectId: AssignmentSubjectId): List<AssignmentSection> {
        return jpaRepository.findByAssignmentSubjectIdOrderBySectionIdAsc(subjectId.value).map {
            converter.toDomain(it)
        }
    }

    override fun save(section: AssignmentSection): AssignmentSection {
        val entity = converter.toEntity(section)
        val saved = jpaRepository.save(entity)
        return converter.toDomain(saved)
    }

    override fun deleteById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId) {
        val entityId = AssignmentSectionEntityId(subjectId.value, sectionId.value)
        jpaRepository.deleteById(entityId)
    }

    override fun existsById(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): Boolean {
        val entityId = AssignmentSectionEntityId(subjectId.value, sectionId.value)
        return jpaRepository.existsById(entityId)
    }
}
