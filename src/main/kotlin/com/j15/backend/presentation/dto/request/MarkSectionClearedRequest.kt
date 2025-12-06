package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min

// セクション完了マークリクエストDTO
data class MarkSectionClearedRequest(
        @field:Min(0, message = "セクションIDは0以上である必要があります")
        @field:Max(100, message = "セクションIDは100以下である必要があります")
        val sectionId: Int
)
