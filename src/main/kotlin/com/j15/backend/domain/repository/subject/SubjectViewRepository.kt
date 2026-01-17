package com.j15.backend.domain.repository.subject

import com.j15.backend.domain.model.ranking.SubjectView
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId

/** 題材閲覧記録リポジトリ（ドメイン層） */
interface SubjectViewRepository {
    fun save(view: SubjectView)
    fun countBySubjectId(subjectId: SubjectId): Long
    fun findTopSubjectIdsByViewCount(limit: Int): List<Pair<SubjectId, Long>>
    fun existsBySubjectIdAndUserId(subjectId: SubjectId, userId: UserId): Boolean
}
