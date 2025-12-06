package com.j15.backend.domain.model.progress

import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import java.time.Instant

// ユーザー完了記録エンティティ（ドメイン層）
// ユーザーが特定の題材の特定セクションを完了したことを記録する
data class UserClearedSection(
        val userClearedSectionId: UserClearedSectionId?,
        val userId: UserId,
        val subjectId: SubjectId,
        val sectionId: SectionId,
        val completedAt: Instant = Instant.now()
) {
    companion object {
        // 新規作成用（IDはDBで自動採番）
        fun create(userId: UserId, subjectId: SubjectId, sectionId: SectionId): UserClearedSection {
            return UserClearedSection(
                    userClearedSectionId = null,
                    userId = userId,
                    subjectId = subjectId,
                    sectionId = sectionId,
                    completedAt = Instant.now()
            )
        }
    }
}
