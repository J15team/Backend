package com.j15.backend.domain.model.section

// セクションエンティティ（ドメイン層）
// アプリ開発の各段階を表す（0~100の進捗ステップ）
data class Section(val sectionId: SectionId, val title: String, val description: String? = null) {
    init {
        require(title.isNotBlank()) { "セクションのタイトルは空にできません" }
    }

    companion object {
        const val MIN_SECTION_ID = 0
        const val MAX_SECTION_ID = 100
    }
}
