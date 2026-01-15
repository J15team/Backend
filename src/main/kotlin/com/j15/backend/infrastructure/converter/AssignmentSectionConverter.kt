package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.assignment.AssignmentSection
import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.infrastructure.entity.AssignmentSectionJpaEntity
import org.springframework.stereotype.Component

@Component
class AssignmentSectionConverter {

    fun toDomain(entity: AssignmentSectionJpaEntity): AssignmentSection {
        return AssignmentSection(
                assignmentSubjectId = AssignmentSubjectId(entity.assignmentSubjectId),
                sectionId = AssignmentSectionId(entity.sectionId),
                title = entity.title,
                description = entity.description,
                hasAssignment = entity.hasAssignment,
                testCases = entity.testCases,
                timeLimit = entity.timeLimit,
                memoryLimit = entity.memoryLimit
        )
    }

    fun toEntity(domain: AssignmentSection): AssignmentSectionJpaEntity {
        return AssignmentSectionJpaEntity(
                assignmentSubjectId = domain.assignmentSubjectId.value,
                sectionId = domain.sectionId.value,
                title = domain.title,
                description = domain.description,
                hasAssignment = domain.hasAssignment,
                testCases = domain.testCases,
                timeLimit = domain.timeLimit,
                memoryLimit = domain.memoryLimit
        )
    }
}
