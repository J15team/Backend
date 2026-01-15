package com.j15.backend.infrastructure.converter

import com.j15.backend.domain.model.assignment.*
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.infrastructure.entity.SubmissionJpaEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.springframework.stereotype.Component

@Component
class SubmissionConverter {

    fun toDomain(entity: SubmissionJpaEntity): Submission {
        return Submission(
                id = SubmissionId(entity.id),
                userId = UserId(entity.userId),
                assignmentSubjectId = AssignmentSubjectId(entity.assignmentSubjectId),
                sectionId = AssignmentSectionId(entity.sectionId),
                code = entity.code,
                language = Language.valueOf(entity.language),
                submittedAt = entity.submittedAt.toInstant(ZoneOffset.UTC),
                status = SubmissionStatus.valueOf(entity.status),
                score = entity.score,
                totalTestCases = entity.totalTestCases,
                passedTestCases = entity.passedTestCases
        )
    }

    fun toEntity(domain: Submission): SubmissionJpaEntity {
        return SubmissionJpaEntity(
                id = domain.id.value,
                userId = domain.userId.value,
                assignmentSubjectId = domain.assignmentSubjectId.value,
                sectionId = domain.sectionId.value,
                code = domain.code,
                language = domain.language.name,
                submittedAt = LocalDateTime.ofInstant(domain.submittedAt, ZoneOffset.UTC),
                status = domain.status.name,
                score = domain.score,
                totalTestCases = domain.totalTestCases,
                passedTestCases = domain.passedTestCases
        )
    }
}
