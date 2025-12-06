package com.j15.backend.infrastructure.persistence.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
        name = "user_cleared_sections",
        uniqueConstraints =
                [
                        UniqueConstraint(
                                name = "uk_user_subject_section",
                                columnNames = ["user_id", "subject_id", "section_id"]
                        )]
)
data class UserClearedSectionEntity(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "user_cleared_section_id")
        val userClearedSectionId: Int? = null,
        @Column(name = "user_id", nullable = false) val userId: UUID = UUID.randomUUID(),
        @Column(name = "subject_id", nullable = false) val subjectId: Long = 0L,
        @Column(name = "section_id", nullable = false) val sectionId: Int = 0,
        @Column(name = "completed_at", nullable = false) val completedAt: Instant = Instant.now()
)
