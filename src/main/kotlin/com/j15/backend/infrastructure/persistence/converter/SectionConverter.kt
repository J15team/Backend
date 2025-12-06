package com.j15.backend.infrastructure.persistence.converter

import com.j15.backend.domain.model.section.Section
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.infrastructure.persistence.entity.SectionEntity

object SectionConverter {
    fun toDomain(entity: SectionEntity): Section {
        return Section(
                subjectId = SubjectId(entity.subjectId),
                sectionId = SectionId(entity.sectionId!!),
                title = entity.title,
                description = entity.description
        )
    }

    fun toEntity(domain: Section): SectionEntity {
        return SectionEntity(
                subjectId = domain.subjectId.value,
                sectionId = domain.sectionId.value,
                title = domain.title,
                description = domain.description
        )
    }
}
