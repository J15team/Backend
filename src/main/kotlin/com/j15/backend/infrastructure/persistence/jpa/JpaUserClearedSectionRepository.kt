package com.j15.backend.infrastructure.persistence.jpa

import com.j15.backend.infrastructure.persistence.entity.UserClearedSectionEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserClearedSectionRepository : JpaRepository<UserClearedSectionEntity, Int> {
    fun findByUserId(userId: UUID): List<UserClearedSectionEntity>
    fun existsByUserIdAndSectionId(userId: UUID, sectionId: Int): Boolean
    fun deleteByUserIdAndSectionId(userId: UUID, sectionId: Int)
}
