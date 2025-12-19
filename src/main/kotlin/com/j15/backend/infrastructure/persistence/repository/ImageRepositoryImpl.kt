package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.image.Image
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.repository.ImageRepository
import com.j15.backend.infrastructure.persistence.converter.ImageConverter
import com.j15.backend.infrastructure.persistence.jpa.JpaImageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

/**
 * 画像リポジトリ実装
 */
@Repository
class ImageRepositoryImpl(private val jpaImageRepository: JpaImageRepository) : ImageRepository {

    override fun findById(imageId: ImageId): Image? {
        return jpaImageRepository.findByIdOrNull(imageId.value)?.let { ImageConverter.toDomain(it) }
    }

    override fun findAllBySectionId(subjectId: SubjectId, sectionId: SectionId): List<Image> {
        return jpaImageRepository
            .findBySubjectIdAndSectionIdOrderByCreatedAtAsc(subjectId.value, sectionId.value)
            .map { ImageConverter.toDomain(it) }
    }

    override fun findAllBySubjectId(subjectId: SubjectId): List<Image> {
        return jpaImageRepository.findBySubjectIdOrderByCreatedAtAsc(subjectId.value).map {
            ImageConverter.toDomain(it)
        }
    }

    override fun save(image: Image): Image {
        val entity = ImageConverter.toEntity(image)
        val saved = jpaImageRepository.save(entity)
        return ImageConverter.toDomain(saved)
    }

    override fun deleteById(imageId: ImageId) {
        jpaImageRepository.deleteById(imageId.value)
    }

    override fun deleteAllBySectionId(subjectId: SubjectId, sectionId: SectionId) {
        jpaImageRepository.deleteBySubjectIdAndSectionId(subjectId.value, sectionId.value)
    }

    override fun deleteAllBySubjectId(subjectId: SubjectId) {
        jpaImageRepository.deleteBySubjectId(subjectId.value)
    }

    override fun countBySectionId(subjectId: SubjectId, sectionId: SectionId): Int {
        return jpaImageRepository.countBySubjectIdAndSectionId(subjectId.value, sectionId.value)
            .toInt()
    }
}
