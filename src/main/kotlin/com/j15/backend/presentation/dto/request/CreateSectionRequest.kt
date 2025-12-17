package com.j15.backend.presentation.dto.request

import org.springframework.web.multipart.MultipartFile

/**
 * セクション作成リクエスト
 * SpringのModelAttributeでバインドできるよう、引数なしコンストラクタ + var プロパティで定義
 */
class CreateSectionRequest {
        var sectionId: Int? = null
        var title: String? = null
        var description: String? = null
        var image: MultipartFile? = null
}
