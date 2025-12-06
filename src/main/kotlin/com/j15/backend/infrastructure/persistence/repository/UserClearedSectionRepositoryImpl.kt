package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
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

    override fun findByUserId(userId: UserId): List<UserClearedSection> {
        return jpaUserClearedSectionRepository.findByUserId(userId.value).map {
            UserClearedSectionConverter.toDomain(it)
        }
    }

    override fun existsByUserIdAndSectionId(userId: UserId, sectionId: SectionId): Boolean {
        return jpaUserClearedSectionRepository.existsByUserIdAndSectionId(
                userId.value,
                sectionId.value
        )
    }

    override fun deleteByUserIdAndSectionId(userId: UserId, sectionId: SectionId) {
        jpaUserClearedSectionRepository.deleteByUserIdAndSectionId(userId.value, sectionId.value)
    }
}
