package com.j15.backend.infrastructure.repository

import com.j15.backend.domain.model.ranking.TagView
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.TagViewRepository
import com.j15.backend.infrastructure.entity.TagViewJpaEntity
import com.j15.backend.infrastructure.repository.jpa.JpaTagViewRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.time.ZoneId

@Repository
class TagViewRepositoryImpl(
    private val jpaRepository: JpaTagViewRepository
) : TagViewRepository {

    override fun save(view: TagView) {
        val entity = TagViewJpaEntity(
            tagId = view.tagId.value,
            userId = view.userId.value,
            viewedAt = LocalDateTime.ofInstant(view.viewedAt, ZoneId.systemDefault())
        )
        jpaRepository.save(entity)
    }

    override fun countByTagId(tagId: TagId): Long {
        return jpaRepository.countByTagId(tagId.value)
    }

    override fun findTopTagIdsByViewCount(limit: Int): List<Pair<TagId, Long>> {
        return jpaRepository.findTopTagIdsByViewCount(limit).map { row ->
            val tagIdValue = row[0] as Long
            val viewCount = row[1] as Long
            TagId(tagIdValue) to viewCount
        }
    }

    override fun existsByTagIdAndUserId(tagId: TagId, userId: UserId): Boolean {
        return jpaRepository.existsByTagIdAndUserId(tagId.value, userId.value)
    }
}
