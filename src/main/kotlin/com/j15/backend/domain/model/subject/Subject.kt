package com.j15.backend.domain.model.subject

import java.time.Instant

// 題材エンティティ（ドメイン層）
// 学習プロジェクトや題材を表す（例: Webアプリ開発、モバイルアプリ開発）
data class Subject(
        val subjectId: SubjectId,
        val title: String,
        val description: String? = null,
        val maxSections: Int, // この題材のセクション最大数
        val createdAt: Instant = Instant.now()
) {
    init {
        require(title.isNotBlank()) { "題材のタイトルは空にできません" }
        require(maxSections in MIN_MAX_SECTIONS..MAX_MAX_SECTIONS) {
            "最大セクション数は${MIN_MAX_SECTIONS}以上${MAX_MAX_SECTIONS}以下である必要があります"
        }
    }

    companion object {
        const val MIN_MAX_SECTIONS = 1
        const val MAX_MAX_SECTIONS = 1000 // 最大1000セクションまで
    }
}
