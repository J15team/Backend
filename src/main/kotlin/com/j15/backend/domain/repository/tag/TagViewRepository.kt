package com.j15.backend.domain.repository.tag

import com.j15.backend.domain.model.ranking.TagView
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.user.UserId

/** タグ閲覧記録リポジトリ（ドメイン層） */
interface TagViewRepository {
    fun save(view: TagView)
    fun countByTagId(tagId: TagId): Long
    fun findTopTagIdsByViewCount(limit: Int): List<Pair<TagId, Long>>
    fun existsByTagIdAndUserId(tagId: TagId, userId: UserId): Boolean
}
