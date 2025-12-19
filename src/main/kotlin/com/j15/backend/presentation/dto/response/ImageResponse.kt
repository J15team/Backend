package com.j15.backend.presentation.dto.response

import com.j15.backend.domain.model.image.Image
import java.time.Instant

/**
 * 画像情報レスポンスDTO
 */
data class ImageResponse(
    val imageId: Long,
    val subjectId: Long,
    val sectionId: Int,
    val imageUrl: String,
    val createdAt: Instant
) {
    companion object {
        fun from(image: Image): ImageResponse {
            return ImageResponse(
                imageId =
                    image.imageId?.value
                        ?: throw IllegalStateException("画像IDが存在しません"),
                subjectId = image.subjectId.value,
                sectionId = image.sectionId.value,
                imageUrl = image.imageUrl,
                createdAt = image.createdAt
            )
        }
    }
}
