package com.j15.backend.domain.model.section

import com.j15.backend.domain.model.subject.SubjectId

// セクションエンティティ（ドメイン層）
// 題材内の各学習ステップを表す
data class Section(
        val subjectId: SubjectId,
        val sectionId: SectionId,
        val title: String,
        val description: String? = null
) {
    init {
        require(title.isNotBlank()) { "セクションのタイトルは空にできません" }
    }

    companion object {
        const val MIN_SECTION_ID = 0
        const val MAX_SECTION_ID = 100
    }
}
