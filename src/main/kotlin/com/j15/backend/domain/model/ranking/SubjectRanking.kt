package com.j15.backend.domain.model.ranking

import com.j15.backend.domain.model.subject.Subject

/**
 * 題材ランキングエントリ（ドメイン層）
 * 題材とその閲覧数を含むランキング情報
 */
data class SubjectRanking(
    val subject: Subject,
    val viewCount: Long,
    val rank: Int
)
