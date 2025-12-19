package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant

/**
 * 画像エンティティ（インフラ層）
 */
@Entity
@Table(name = "images")
data class ImageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    val imageId: Long? = null,

    @Column(name = "subject_id", nullable = false)
    val subjectId: Long = 0L,

    @Column(name = "section_id", nullable = false)
    val sectionId: Int = 0,

    @Column(name = "image_url", nullable = false, length = 2048)
    val imageUrl: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)
