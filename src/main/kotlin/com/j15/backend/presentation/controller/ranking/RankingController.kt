package com.j15.backend.presentation.controller.ranking

import com.j15.backend.application.usecase.ranking.RankingUseCase
import com.j15.backend.presentation.dto.ranking.SubjectRankingResponse
import com.j15.backend.presentation.dto.ranking.TagRankingResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * ランキング取得コントローラー
 * 責務: 題材・タグのランキング情報の取得
 */
@RestController
@RequestMapping("/api/rankings")
class RankingController(
    private val rankingUseCase: RankingUseCase
) {
    /**
     * 題材のランキングを取得する
     * @param limit 取得件数（デフォルト: 10、最大: 100）
     */
    @GetMapping("/subjects")
    fun getSubjectRanking(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<SubjectRankingResponse>> {
        val rankings = rankingUseCase.getSubjectRanking(limit)
        return ResponseEntity.ok(rankings.map { SubjectRankingResponse.from(it) })
    }

    /**
     * タグのランキングを取得する
     * @param limit 取得件数（デフォルト: 10、最大: 100）
     */
    @GetMapping("/tags")
    fun getTagRanking(
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<TagRankingResponse>> {
        val rankings = rankingUseCase.getTagRanking(limit)
        return ResponseEntity.ok(rankings.map { TagRankingResponse.from(it) })
    }
}
