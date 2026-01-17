package com.j15.backend.infrastructure.repository.assignment

import com.j15.backend.domain.model.assignment.AssignmentSubject
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.repository.assignment.AssignmentSubjectRepository
import com.j15.backend.infrastructure.converter.AssignmentSubjectConverter
import com.j15.backend.infrastructure.repository.jpa.JpaAssignmentSubjectRepository
import org.springframework.stereotype.Repository

@Repository
class AssignmentSubjectRepositoryImpl(
        private val jpaRepository: JpaAssignmentSubjectRepository,
        private val converter: AssignmentSubjectConverter
) : AssignmentSubjectRepository {

    override fun findById(id: AssignmentSubjectId): AssignmentSubject? {
        return jpaRepository.findById(id.value).map { converter.toDomain(it) }.orElse(null)
    }

    override fun findAll(): List<AssignmentSubject> {
        return jpaRepository.findAllByOrderByIdAsc().map { converter.toDomain(it) }
    }

    override fun save(subject: AssignmentSubject): AssignmentSubject {
        val entity = converter.toEntity(subject)
        val saved = jpaRepository.save(entity)
        return converter.toDomain(saved)
    }

    override fun deleteById(id: AssignmentSubjectId) {
        jpaRepository.deleteById(id.value)
    }

    override fun existsById(id: AssignmentSubjectId): Boolean {
        return jpaRepository.existsById(id.value)
    }
}
