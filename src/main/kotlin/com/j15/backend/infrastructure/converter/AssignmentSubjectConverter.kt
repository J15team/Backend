package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.assignment.AssignmentSubject
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.infrastructure.entity.AssignmentSubjectJpaEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.stereotype.Component

@Component
class AssignmentSubjectConverter {

    fun toDomain(entity: AssignmentSubjectJpaEntity): AssignmentSubject {
        return AssignmentSubject(
                id = AssignmentSubjectId(entity.id),
                title = entity.title,
                description = entity.description,
                maxSections = entity.maxSections,
                weight = entity.weight,
                createdAt = entity.createdAt.toInstant(ZoneOffset.UTC)
        )
    }

    fun toEntity(domain: AssignmentSubject): AssignmentSubjectJpaEntity {
        return AssignmentSubjectJpaEntity(
                id = domain.id.value,
                title = domain.title,
                description = domain.description,
                maxSections = domain.maxSections,
                weight = domain.weight,
                createdAt = LocalDateTime.ofInstant(domain.createdAt, ZoneOffset.UTC)
        )
    }
}
