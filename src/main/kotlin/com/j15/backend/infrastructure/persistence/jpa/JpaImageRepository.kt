package com.j15.backend.infrastructure.persistence.jpa

import com.j15.backend.infrastructure.persistence.entity.ImageEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * 画像JPAリポジトリ
 */
interface JpaImageRepository : JpaRepository<ImageEntity, Long> {

    /**
     * 指定セクションの画像一覧を取得（作成日時昇順）
     */
    fun findBySubjectIdAndSectionIdOrderByCreatedAtAsc(
        subjectId: Long,
        sectionId: Int
    ): List<ImageEntity>

    /**
     * 指定題材の画像一覧を取得
     */
    fun findBySubjectIdOrderByCreatedAtAsc(subjectId: Long): List<ImageEntity>

    /**
     * 指定セクションの画像数をカウント
     */
    fun countBySubjectIdAndSectionId(subjectId: Long, sectionId: Int): Long

    /**
     * 指定セクションの全画像を削除
     */
    fun deleteBySubjectIdAndSectionId(subjectId: Long, sectionId: Int)

    /**
     * 指定題材の全画像を削除
     */
    fun deleteBySubjectId(subjectId: Long)
}
