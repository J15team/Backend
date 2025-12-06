package com.j15.backend.infrastructure.persistence.jpa

import com.j15.backend.infrastructure.persistence.entity.UserClearedSectionEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserClearedSectionRepository : JpaRepository<UserClearedSectionEntity, Int> {
    /** 指定ユーザーと題材の完了済みセクション一覧を取得 */
    fun findByUserIdAndSubjectId(userId: UUID, subjectId: Long): List<UserClearedSectionEntity>

    /** 指定ユーザー、題材、セクションの完了記録が存在するかチェック */
    fun existsByUserIdAndSubjectIdAndSectionId(
            userId: UUID,
            subjectId: Long,
            sectionId: Int
    ): Boolean

    /** 指定ユーザー、題材、セクションの完了記録を削除 */
    fun deleteByUserIdAndSubjectIdAndSectionId(userId: UUID, subjectId: Long, sectionId: Int)
}
