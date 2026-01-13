package com.j15.backend.presentation.dto.ranking

import com.j15.backend.domain.model.ranking.SubjectRanking
import com.j15.backend.domain.model.ranking.TagRanking
import com.j15.backend.presentation.dto.subject.SubjectResponse
import com.j15.backend.presentation.dto.tag.TagResponse

/**
 * 題材ランキングレスポンス
 */
data class SubjectRankingResponse(
    val rank: Int,
    val viewCount: Long,
    val subject: SubjectResponse
) {
    companion object {
        fun from(ranking: SubjectRanking): SubjectRankingResponse {
            return SubjectRankingResponse(
                rank = ranking.rank,
                viewCount = ranking.viewCount,
                subject = SubjectResponse.from(ranking.subject)
            )
        }
    }
}

/**
 * タグランキングレスポンス
 */
data class TagRankingResponse(
    val rank: Int,
    val viewCount: Long,
    val tag: TagResponse
) {
    companion object {
        fun from(ranking: TagRanking): TagRankingResponse {
            return TagRankingResponse(
                rank = ranking.rank,
                viewCount = ranking.viewCount,
                tag = TagResponse.from(ranking.tag)
            )
        }
    }
}

/**
 * 閲覧記録レスポンス
 */
data class ViewRecordResponse(
    val recorded: Boolean,
    val isNewView: Boolean,
    val message: String
) {
    companion object {
        fun newView(): ViewRecordResponse {
            return ViewRecordResponse(
                recorded = true,
                isNewView = true,
                message = "閲覧を記録しました"
            )
        }

        fun alreadyViewed(): ViewRecordResponse {
            return ViewRecordResponse(
                recorded = true,
                isNewView = false,
                message = "既に閲覧済みです（閲覧日時を更新しました）"
            )
        }
    }
}
