package com.j15.backend.domain.model.ranking

import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.user.UserId
import java.time.Instant

/**
 * 題材閲覧記録（ドメイン層）
 * ユーザーが題材を閲覧した記録を表す
 * 同一ユーザーからは1回のみカウントされる
 */
data class SubjectView(
    val subjectId: SubjectId,
    val userId: UserId,
    val viewedAt: Instant = Instant.now()
)
