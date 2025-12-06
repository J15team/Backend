package com.j15.backend.infrastructure.persistence.converter

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.infrastructure.persistence.entity.UserClearedSectionEntity

object UserClearedSectionConverter {
    fun toDomain(entity: UserClearedSectionEntity): UserClearedSection {
        return UserClearedSection(
                userClearedSectionId =
                        entity.userClearedSectionId?.let { UserClearedSectionId(it) },
                userId = UserId(entity.userId),
                subjectId = SubjectId(entity.subjectId),
                sectionId = SectionId(entity.sectionId),
                completedAt = entity.completedAt
        )
    }

    fun toEntity(domain: UserClearedSection): UserClearedSectionEntity {
        return UserClearedSectionEntity(
                userClearedSectionId = domain.userClearedSectionId?.value,
                userId = domain.userId.value,
                subjectId = domain.subjectId.value,
                sectionId = domain.sectionId.value,
                completedAt = domain.completedAt
        )
    }
}
