package com.j15.backend.domain.repository.image

import com.j15.backend.domain.model.image.Image
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId

/** 画像リポジトリ（ドメイン層のインターフェース） */
interface ImageRepository {
    fun findById(imageId: ImageId): Image?
    fun findAllBySectionId(subjectId: SubjectId, sectionId: SectionId): List<Image>
    fun findAllBySubjectId(subjectId: SubjectId): List<Image>
    fun save(image: Image): Image
    fun deleteById(imageId: ImageId)
    fun deleteAllBySectionId(subjectId: SubjectId, sectionId: SectionId)
    fun deleteAllBySubjectId(subjectId: SubjectId)
    fun countBySectionId(subjectId: SubjectId, sectionId: SectionId): Int
}
