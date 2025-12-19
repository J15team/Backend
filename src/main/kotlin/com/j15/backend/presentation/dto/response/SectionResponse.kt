package com.j15.backend.presentation.dto.response

import com.j15.backend.domain.model.section.Section

// セクション情報レスポンスDTO
data class SectionResponse(
        val subjectId: Long,
        val sectionId: Int,
        val title: String,
        val description: String?,
        val images: List<ImageResponse>? = null
) {
    companion object {
        fun from(section: Section, images: List<ImageResponse>? = null): SectionResponse {
            return SectionResponse(
                    subjectId = section.subjectId.value,
                    sectionId = section.sectionId.value,
                    title = section.title,
                    description = section.description,
                    images = images
            )
        }
    }
}
