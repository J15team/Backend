package com.j15.backend.infrastructure.repository.subject

import com.j15.backend.domain.model.ranking.SubjectView
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.subject.SubjectViewRepository
import com.j15.backend.infrastructure.entity.SubjectViewJpaEntity
import com.j15.backend.infrastructure.repository.jpa.JpaSubjectViewRepository
import java.time.LocalDateTime
import java.time.ZoneId
import org.springframework.stereotype.Repository

@Repository
class SubjectViewRepositoryImpl(private val jpaRepository: JpaSubjectViewRepository) :
        SubjectViewRepository {

    override fun save(view: SubjectView) {
        val entity =
                SubjectViewJpaEntity(
                        subjectId = view.subjectId.value,
                        userId = view.userId.value,
                        viewedAt = LocalDateTime.ofInstant(view.viewedAt, ZoneId.systemDefault())
                )
        jpaRepository.save(entity)
    }

    override fun countBySubjectId(subjectId: SubjectId): Long {
        return jpaRepository.countBySubjectId(subjectId.value)
    }

    override fun findTopSubjectIdsByViewCount(limit: Int): List<Pair<SubjectId, Long>> {
        return jpaRepository.findTopSubjectIdsByViewCount(limit).map { row ->
            val subjectIdValue = row[0] as Long
            val viewCount = row[1] as Long
            SubjectId(subjectIdValue) to viewCount
        }
    }

    override fun existsBySubjectIdAndUserId(subjectId: SubjectId, userId: UserId): Boolean {
        return jpaRepository.existsBySubjectIdAndUserId(subjectId.value, userId.value)
    }
}
