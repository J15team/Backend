package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*

@Entity
@Table(name = "sections")
data class SectionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "section_id")
        val sectionId: Int? = null,
        @Column(name = "title", nullable = false) val title: String = "",
        @Column(name = "description") val description: String? = null,
        @Column(name = "order_index", nullable = false) val orderIndex: Int = 0
)
