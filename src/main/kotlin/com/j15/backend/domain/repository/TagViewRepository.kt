package com.j15.backend.domain.repository

import com.j15.backend.domain.model.ranking.TagView
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.user.UserId

/**
 * タグ閲覧記録リポジトリ（ドメイン層）
 */
interface TagViewRepository {
    /**
     * 閲覧記録を保存する（既存の場合は更新）
     */
    fun save(view: TagView)

    /**
     * 特定のタグの閲覧数を取得する
     */
    fun countByTagId(tagId: TagId): Long

    /**
     * 閲覧数の多い順にタグIDと閲覧数を取得する
     * @param limit 取得する件数
     * @return タグIDと閲覧数のペアのリスト
     */
    fun findTopTagIdsByViewCount(limit: Int): List<Pair<TagId, Long>>

    /**
     * 特定のユーザーが特定のタグを閲覧済みかどうかを確認する
     */
    fun existsByTagIdAndUserId(tagId: TagId, userId: UserId): Boolean
}
