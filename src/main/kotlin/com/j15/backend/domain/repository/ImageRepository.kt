package com.j15.backend.domain.repository

import com.j15.backend.domain.model.image.Image
import com.j15.backend.domain.model.image.ImageId
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId

/**
 * 画像リポジトリ（ドメイン層のインターフェース）
 */
interface ImageRepository {
    /**
     * 画像IDで画像を取得
     */
    fun findById(imageId: ImageId): Image?

    /**
     * 指定セクションの画像一覧を取得
     */
    fun findAllBySectionId(subjectId: SubjectId, sectionId: SectionId): List<Image>

    /**
     * 指定題材の画像一覧を取得
     */
    fun findAllBySubjectId(subjectId: SubjectId): List<Image>

    /**
     * 画像を保存
     */
    fun save(image: Image): Image

    /**
     * 画像IDで画像を削除
     */
    fun deleteById(imageId: ImageId)

    /**
     * 指定セクションの全画像を削除
     */
    fun deleteAllBySectionId(subjectId: SubjectId, sectionId: SectionId)

    /**
     * 指定題材の全画像を削除
     */
    fun deleteAllBySubjectId(subjectId: SubjectId)

    /**
     * 指定セクションの画像数をカウント
     */
    fun countBySectionId(subjectId: SubjectId, sectionId: SectionId): Int
}
