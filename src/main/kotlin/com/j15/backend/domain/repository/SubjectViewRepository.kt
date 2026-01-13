package com.j15.backend.domain.repository

import com.j15.backend.domain.model.ranking.SubjectView
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId

/**
 * 題材閲覧記録リポジトリ（ドメイン層）
 */
interface SubjectViewRepository {
    /**
     * 閲覧記録を保存する（既存の場合は更新）
     */
    fun save(view: SubjectView)

    /**
     * 特定の題材の閲覧数を取得する
     */
    fun countBySubjectId(subjectId: SubjectId): Long

    /**
     * 閲覧数の多い順に題材IDと閲覧数を取得する
     * @param limit 取得する件数
     * @return 題材IDと閲覧数のペアのリスト
     */
    fun findTopSubjectIdsByViewCount(limit: Int): List<Pair<SubjectId, Long>>

    /**
     * 特定のユーザーが特定の題材を閲覧済みかどうかを確認する
     */
    fun existsBySubjectIdAndUserId(subjectId: SubjectId, userId: UserId): Boolean
}
