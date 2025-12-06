package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.infrastructure.entity.SubjectJpaEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.stereotype.Component

@Component
class SubjectConverter {

    fun toDomain(entity: SubjectJpaEntity): Subject {
        return Subject(
                subjectId = SubjectId(entity.subjectId),
                title = entity.title,
                description = entity.description,
                maxSections = entity.maxSections,
                createdAt = entity.createdAt.toInstant(ZoneOffset.UTC)
        )
    }
    fun toEntity(domain: Subject): SubjectJpaEntity {
        return SubjectJpaEntity(
                subjectId = domain.subjectId.value,
                title = domain.title,
                description = domain.description,
                maxSections = domain.maxSections,
                createdAt = LocalDateTime.ofInstant(domain.createdAt, ZoneOffset.UTC)
        )
    }
}
