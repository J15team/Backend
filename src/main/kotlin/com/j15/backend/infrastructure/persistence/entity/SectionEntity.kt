package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.io.Serializable

/** セクションの複合主キークラス DBの主キー (subject_id, section_id) に対応 */
@Embeddable
data class SectionEntityId(
        @Column(name = "subject_id") val subjectId: Long = 0L,
        @Column(name = "section_id") val sectionId: Int = 0
) : Serializable

@Entity
@Table(name = "sections")
@IdClass(SectionEntityId::class)
data class SectionEntity(
        @Id @Column(name = "subject_id", nullable = false) val subjectId: Long = 0L,
        @Id @Column(name = "section_id", nullable = false) val sectionId: Int = 0,
        @Column(name = "title", nullable = false) val title: String = "",
        @Column(name = "description") val description: String? = null
)
