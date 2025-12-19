package com.j15.backend.domain.model.image

import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import java.time.Instant

/**
 * 画像エンティティ（ドメイン層）
 * セクションに関連付けられた画像を表す
 */
data class Image(
    val imageId: ImageId?,  // 新規作成時はnull
    val subjectId: SubjectId,
    val sectionId: SectionId,
    val imageUrl: String,
    val createdAt: Instant = Instant.now()
) {
    init {
        require(imageUrl.isNotBlank()) { "画像URLは空にできません" }
        require(imageUrl.startsWith("https://")) { "画像URLはHTTPSで始まる必要があります" }
    }
}
