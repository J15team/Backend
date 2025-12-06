package com.j15.backend.domain.repository

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId

// ユーザー完了記録リポジトリ（ドメイン層のインターフェース）
interface UserClearedSectionRepository {
    fun save(userClearedSection: UserClearedSection): UserClearedSection
    fun findById(id: UserClearedSectionId): UserClearedSection?
    fun findByUserId(userId: UserId): List<UserClearedSection>
    fun existsByUserIdAndSectionId(userId: UserId, sectionId: SectionId): Boolean
    fun deleteByUserIdAndSectionId(userId: UserId, sectionId: SectionId)
}
