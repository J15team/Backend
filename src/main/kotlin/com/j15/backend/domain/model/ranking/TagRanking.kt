package com.j15.backend.domain.model.ranking

import com.j15.backend.domain.model.tag.Tag

/**
 * タグランキングエントリ（ドメイン層）
 * タグとその閲覧数を含むランキング情報
 */
data class TagRanking(
    val tag: Tag,
    val viewCount: Long,
    val rank: Int
)
