package com.j15.backend.domain.repository.user

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId

/** ユーザー完了記録リポジトリ（ドメイン層のインターフェース） */
interface UserClearedSectionRepository {
    fun save(userClearedSection: UserClearedSection): UserClearedSection
    fun findById(id: UserClearedSectionId): UserClearedSection?
    fun findByUserIdAndSubjectId(userId: UserId, subjectId: SubjectId): List<UserClearedSection>
    fun existsByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ): Boolean
    fun deleteByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    )
}
