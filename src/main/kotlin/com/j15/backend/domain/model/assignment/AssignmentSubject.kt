package com.j15.backend.domain.model.assignment

import java.time.Instant

/** 課題題材エンティティ（ドメイン層） 既存のSubjectと同じ仕様で、課題専用の題材を表す */
data class AssignmentSubject(
        val id: AssignmentSubjectId,
        val title: String,
        val description: String? = null,
        val maxSections: Int,
        val weight: Int,
        val createdAt: Instant = Instant.now()
) {
    init {
        require(title.isNotBlank()) { "課題題材のタイトルは空にできません" }
        require(maxSections in MIN_MAX_SECTIONS..MAX_MAX_SECTIONS) {
            "最大セクション数は${MIN_MAX_SECTIONS}以上${MAX_MAX_SECTIONS}以下である必要があります"
        }
        require(weight in MIN_WEIGHT..MAX_WEIGHT) { "重みは${MIN_WEIGHT}以上${MAX_WEIGHT}以下である必要があります" }
    }

    companion object {
        const val MIN_MAX_SECTIONS = 1
        const val MAX_MAX_SECTIONS = 1000
        const val MIN_WEIGHT = 1
        const val MAX_WEIGHT = 5
    }
}
