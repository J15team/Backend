package com.j15.backend.domain.repository

import com.j15.backend.domain.model.progress.UserClearedSection
import com.j15.backend.domain.model.progress.UserClearedSectionId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId

// ユーザー完了記録リポジトリ（ドメイン層のインターフェース）
interface UserClearedSectionRepository {
    fun save(userClearedSection: UserClearedSection): UserClearedSection
    fun findById(id: UserClearedSectionId): UserClearedSection?

    /** 指定ユーザーと題材の完了済みセクション一覧を取得 */
    fun findByUserIdAndSubjectId(userId: UserId, subjectId: SubjectId): List<UserClearedSection>

    /** 指定ユーザー、題材、セクションの完了記録が存在するかチェック */
    fun existsByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    ): Boolean

    /** 指定ユーザー、題材、セクションの完了記録を削除 */
    fun deleteByUserIdAndSubjectIdAndSectionId(
            userId: UserId,
            subjectId: SubjectId,
            sectionId: SectionId
    )
}
