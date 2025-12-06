package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.infrastructure.persistence.converter.SectionConverter
import com.j15.backend.infrastructure.persistence.jpa.JpaSectionRepository
import org.springframework.stereotype.Repository

@Repository
class SectionRepositoryImpl(private val jpaSectionRepository: JpaSectionRepository) :
        SectionRepository {

    override fun findById(subjectId: SubjectId, sectionId: SectionId): Section? {
        return jpaSectionRepository.findBySubjectIdAndSectionId(subjectId.value, sectionId.value)
                ?.let { SectionConverter.toDomain(it) }
    }

    override fun findAllBySubjectId(subjectId: SubjectId): List<Section> {
        return jpaSectionRepository.findBySubjectId(subjectId.value).map {
            SectionConverter.toDomain(it)
        }
    }

    override fun save(section: Section): Section {
        val entity = SectionConverter.toEntity(section)
        val saved = jpaSectionRepository.save(entity)
        return SectionConverter.toDomain(saved)
    }

    override fun existsById(subjectId: SubjectId, sectionId: SectionId): Boolean {
        return jpaSectionRepository.findBySubjectIdAndSectionId(subjectId.value, sectionId.value) !=
                null
    }

    override fun countBySubjectId(subjectId: SubjectId): Int {
        return jpaSectionRepository.countBySubjectId(subjectId.value).toInt()
    }
}
