package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "sections")
data class SectionEntity(
        @Column(name = "subject_id", nullable = false) val subjectId: Long = 0L,
        @Id @Column(name = "section_id") val sectionId: Int? = null,
        @Column(name = "title", nullable = false) val title: String = "",
        @Column(name = "description") val description: String? = null,
        @Column(name = "image_url") val imageUrl: String? = null
)
