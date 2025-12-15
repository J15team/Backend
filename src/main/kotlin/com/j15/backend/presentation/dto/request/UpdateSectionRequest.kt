package com.j15.backend.presentation.dto.request

import org.springframework.web.multipart.MultipartFile

/**
 * セクション更新リクエスト
 */
data class UpdateSectionRequest(
        val title: String? = null,
        val description: String? = null,
        val image: MultipartFile? = null
)

