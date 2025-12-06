package com.j15.backend.infrastructure.repository

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.infrastructure.converter.SubjectConverter
import com.j15.backend.infrastructure.repository.jpa.JpaSubjectRepository
import org.springframework.stereotype.Repository

@Repository
class SubjectRepositoryImpl(
        private val jpaSubjectRepository: JpaSubjectRepository,
        private val subjectConverter: SubjectConverter
) : SubjectRepository {

    override fun findById(subjectId: SubjectId): Subject? {
        return jpaSubjectRepository
                .findById(subjectId.value)
                .map { subjectConverter.toDomain(it) }
                .orElse(null)
    }

    override fun findAll(): List<Subject> {
        return jpaSubjectRepository.findAll().map { subjectConverter.toDomain(it) }
    }

    override fun save(subject: Subject): Subject {
        val entity = subjectConverter.toEntity(subject)
        val saved = jpaSubjectRepository.save(entity)
        return subjectConverter.toDomain(saved)
    }

    override fun deleteById(subjectId: SubjectId) {
        jpaSubjectRepository.deleteById(subjectId.value)
    }

    override fun existsById(subjectId: SubjectId): Boolean {
        return jpaSubjectRepository.existsById(subjectId.value)
    }
}
