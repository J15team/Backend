package com.j15.backend.presentation.dto.request

import org.springframework.web.multipart.MultipartFile

/**
 * セクション作成リクエスト
 */
data class CreateSectionRequest(
        val sectionId: Int,
        val title: String,
        val description: String? = null,
        val image: MultipartFile? = null
)

