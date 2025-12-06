package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.repository.SectionRepository
import com.j15.backend.infrastructure.persistence.converter.SectionConverter
import com.j15.backend.infrastructure.persistence.jpa.JpaSectionRepository
import org.springframework.stereotype.Repository

@Repository
class SectionRepositoryImpl(private val jpaSectionRepository: JpaSectionRepository) :
        SectionRepository {

    override fun findById(sectionId: SectionId): Section? {
        return jpaSectionRepository
                .findById(sectionId.value)
                .map { SectionConverter.toDomain(it) }
                .orElse(null)
    }

    override fun findAll(): List<Section> {
        return jpaSectionRepository.findAll().map { SectionConverter.toDomain(it) }
    }

    override fun save(section: Section): Section {
        val entity = SectionConverter.toEntity(section)
        val saved = jpaSectionRepository.save(entity)
        return SectionConverter.toDomain(saved)
    }

    override fun existsById(sectionId: SectionId): Boolean {
        return jpaSectionRepository.existsById(sectionId.value)
    }

    override fun count(): Int {
        return jpaSectionRepository.count().toInt()
    }
}
