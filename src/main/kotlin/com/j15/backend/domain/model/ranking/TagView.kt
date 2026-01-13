package com.j15.backend.domain.model.ranking

import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.user.UserId
import java.time.Instant

/**
 * タグ閲覧記録（ドメイン層）
 * ユーザーがタグを閲覧した記録を表す
 * 同一ユーザーからは1回のみカウントされる
 */
data class TagView(
    val tagId: TagId,
    val userId: UserId,
    val viewedAt: Instant = Instant.now()
)
