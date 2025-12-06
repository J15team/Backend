package com.j15.backend.domain.model.progress

import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.user.UserId
import java.time.Instant

// ユーザー完了記録エンティティ（ドメイン層）
// ユーザーが特定のセクションを完了したことを記録する
data class UserClearedSection(
        val userClearedSectionId: UserClearedSectionId?,
        val userId: UserId,
        val sectionId: SectionId,
        val completedAt: Instant = Instant.now()
) {
    companion object {
        // 新規作成用（IDはDBで自動採番）
        fun create(userId: UserId, sectionId: SectionId): UserClearedSection {
            return UserClearedSection(
                    userClearedSectionId = null,
                    userId = userId,
                    sectionId = sectionId,
                    completedAt = Instant.now()
            )
        }
    }
}
