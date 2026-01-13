package com.j15.backend.application.usecase.ranking

import com.j15.backend.domain.model.ranking.SubjectRanking
import com.j15.backend.domain.model.ranking.SubjectView
import com.j15.backend.domain.model.ranking.TagRanking
import com.j15.backend.domain.model.ranking.TagView
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.domain.repository.SubjectViewRepository
import com.j15.backend.domain.repository.TagRepository
import com.j15.backend.domain.repository.TagViewRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * ランキング機能のユースケース
 */
@Service
class RankingUseCase(
    private val subjectViewRepository: SubjectViewRepository,
    private val tagViewRepository: TagViewRepository,
    private val subjectRepository: SubjectRepository,
    private val tagRepository: TagRepository
) {
    companion object {
        const val DEFAULT_RANKING_LIMIT = 10
        const val MAX_RANKING_LIMIT = 100
    }

    /**
     * 題材の閲覧を記録する
     * 同じユーザーからの重複アクセスは上書きされる
     * @return true: 新規記録, false: 既に閲覧済み
     */
    @Transactional
    fun recordSubjectView(subjectId: Long, userId: UserId): Boolean {
        val subjectIdValue = SubjectId(subjectId)

        // 題材が存在するか確認
        if (!subjectRepository.existsById(subjectIdValue)) {
            throw IllegalArgumentException("題材が見つかりません: $subjectId")
        }

        // 既に閲覧済みかチェック
        val alreadyViewed = subjectViewRepository.existsBySubjectIdAndUserId(subjectIdValue, userId)

        // 閲覧記録を保存（既存の場合は更新）
        val view = SubjectView(subjectId = subjectIdValue, userId = userId)
        subjectViewRepository.save(view)

        return !alreadyViewed
    }

    /**
     * タグの閲覧を記録する
     * 同じユーザーからの重複アクセスは上書きされる
     * @return true: 新規記録, false: 既に閲覧済み
     */
    @Transactional
    fun recordTagView(tagId: Long, userId: UserId): Boolean {
        val tagIdValue = TagId(tagId)

        // タグが存在するか確認
        if (!tagRepository.existsById(tagIdValue)) {
            throw IllegalArgumentException("タグが見つかりません: $tagId")
        }

        // 既に閲覧済みかチェック
        val alreadyViewed = tagViewRepository.existsByTagIdAndUserId(tagIdValue, userId)

        // 閲覧記録を保存（既存の場合は更新）
        val view = TagView(tagId = tagIdValue, userId = userId)
        tagViewRepository.save(view)

        return !alreadyViewed
    }

    /**
     * 題材のランキングを取得する
     * @param limit 取得件数（最大100件）
     */
    @Transactional(readOnly = true)
    fun getSubjectRanking(limit: Int = DEFAULT_RANKING_LIMIT): List<SubjectRanking> {
        val effectiveLimit = limit.coerceIn(1, MAX_RANKING_LIMIT)

        val topSubjects = subjectViewRepository.findTopSubjectIdsByViewCount(effectiveLimit)

        return topSubjects.mapIndexedNotNull { index, (subjectId, viewCount) ->
            val subject = subjectRepository.findById(subjectId)
            subject?.let {
                SubjectRanking(
                    subject = it,
                    viewCount = viewCount,
                    rank = index + 1
                )
            }
        }
    }

    /**
     * タグのランキングを取得する
     * @param limit 取得件数（最大100件）
     */
    @Transactional(readOnly = true)
    fun getTagRanking(limit: Int = DEFAULT_RANKING_LIMIT): List<TagRanking> {
        val effectiveLimit = limit.coerceIn(1, MAX_RANKING_LIMIT)

        val topTags = tagViewRepository.findTopTagIdsByViewCount(effectiveLimit)

        return topTags.mapIndexedNotNull { index, (tagId, viewCount) ->
            val tag = tagRepository.findById(tagId)
            tag?.let {
                TagRanking(
                    tag = it,
                    viewCount = viewCount,
                    rank = index + 1
                )
            }
        }
    }

    /**
     * 特定の題材の閲覧数を取得する
     */
    @Transactional(readOnly = true)
    fun getSubjectViewCount(subjectId: Long): Long {
        return subjectViewRepository.countBySubjectId(SubjectId(subjectId))
    }

    /**
     * 特定のタグの閲覧数を取得する
     */
    @Transactional(readOnly = true)
    fun getTagViewCount(tagId: Long): Long {
        return tagViewRepository.countByTagId(TagId(tagId))
    }
}
