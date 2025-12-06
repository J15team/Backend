package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserClearedSectionRepository
import com.j15.backend.infrastructure.persistence.converter.UserClearedSectionConverter
import com.j15.backend.infrastructure.persistence.jpa.JpaUserClearedSectionRepository
import org.springframework.stereotype.Repository

@Repository
class UserClearedSectionRepositoryImpl(
        private val jpaUserClearedSectionRepository: JpaUserClearedSectionRepository
) : UserClearedSectionRepository {

    override fun save(userClearedSection: UserClearedSection): UserClearedSection {
        val entity = UserClearedSectionConverter.toEntity(userClearedSection)
        val saved = jpaUserClearedSectionRepository.save(entity)
        return UserClearedSectionConverter.toDomain(saved)
    }

    override fun findById(id: UserClearedSectionId): UserClearedSection? {
        return jpaUserClearedSectionRepository
                .findById(id.value)
                .map { UserClearedSectionConverter.toDomain(it) }
                .orElse(null)
    }

    override fun findByUserIdAndSubjectId(
            userId: UserId,
            subjectId: SubjectId
    ): List<UserClearedSection> {
        return jpaUserClearedSectionRepository.findByUserIdAndSubjectId(
                        userId.value,
                        subjectId.value
                )
                .map { UserClearedSectionConverter.toDomain(it) }
    }

    override fun existsByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ): Boolean {
        return jpaUserClearedSectionRepository.existsByUserIdAndSubjectIdAndSectionId(
                userId.value,
                subjectId.value,
                sectionId.value
        )
    }

    override fun deleteByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ) {
        jpaUserClearedSectionRepository.deleteByUserIdAndSubjectIdAndSectionId(
                userId.value,
                subjectId.value,
                sectionId.value
        )
    }
}
