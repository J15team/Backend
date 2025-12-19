package com.j15.backend.presentation.dto.request

import org.springframework.web.multipart.MultipartFile

/**
 * セクション更新リクエスト
 */
class UpdateSectionRequest {
    var title: String? = null
    var description: String? = null
    var image: MultipartFile? = null
    var deleteImage: Boolean? = false
}
