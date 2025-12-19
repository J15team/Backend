package com.j15.backend.infrastructure.persistence.converter

import com.j15.backend.domain.model.image.Image
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.infrastructure.persistence.entity.ImageEntity

/**
 * 画像コンバーター
 * ドメインモデルとJPAエンティティの相互変換
 */
object ImageConverter {
    /**
     * JPAエンティティからドメインモデルへ変換
     */
    fun toDomain(entity: ImageEntity): Image {
        return Image(
            imageId = entity.imageId?.let { ImageId(it) },
            subjectId = SubjectId(entity.subjectId),
            sectionId = SectionId(entity.sectionId),
            imageUrl = entity.imageUrl,
            createdAt = entity.createdAt
        )
    }

    /**
     * ドメインモデルからJPAエンティティへ変換
     */
    fun toEntity(domain: Image): ImageEntity {
        return ImageEntity(
            imageId = domain.imageId?.value,
            subjectId = domain.subjectId.value,
            sectionId = domain.sectionId.value,
            imageUrl = domain.imageUrl,
            createdAt = domain.createdAt
        )
    }
}
