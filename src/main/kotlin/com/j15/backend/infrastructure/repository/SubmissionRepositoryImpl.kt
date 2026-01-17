package com.j15.backend.infrastructure.repository

import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.model.assignment.Submission
import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.assignment.SubmissionRepository
import com.j15.backend.infrastructure.converter.SubmissionConverter
import com.j15.backend.infrastructure.repository.jpa.JpaSubmissionRepository
import org.springframework.stereotype.Repository

/** 提出リポジトリ実装 INSERT only（更新・削除メソッドなし） */
@Repository
class SubmissionRepositoryImpl(
        private val jpaRepository: JpaSubmissionRepository,
        private val converter: SubmissionConverter
) : SubmissionRepository {

        override fun findById(id: SubmissionId): Submission? {
                return jpaRepository.findById(id.value).map { converter.toDomain(it) }.orElse(null)
        }

        override fun findByUserAndSection(
                userId: UserId,
                subjectId: AssignmentSubjectId,
                sectionId: AssignmentSectionId
        ): List<Submission> {
                return jpaRepository
                        .findByUserIdAndAssignmentSubjectIdAndSectionIdOrderBySubmittedAtDesc(
                                userId.value,
                                subjectId.value,
                                sectionId.value
                        )
                        .map { converter.toDomain(it) }
        }

        override fun findByUserAndSubject(
                userId: UserId,
                subjectId: AssignmentSubjectId
        ): List<Submission> {
                return jpaRepository.findByUserIdAndAssignmentSubjectIdOrderBySubmittedAtDesc(
                                userId.value,
                                subjectId.value
                        )
                        .map { converter.toDomain(it) }
        }

        override fun findBySection(
                subjectId: AssignmentSubjectId,
                sectionId: AssignmentSectionId
        ): List<Submission> {
                return jpaRepository.findByAssignmentSubjectIdAndSectionIdOrderBySubmittedAtDesc(
                                subjectId.value,
                                sectionId.value
                        )
                        .map { converter.toDomain(it) }
        }

        override fun findBySubject(subjectId: AssignmentSubjectId): List<Submission> {
                return jpaRepository.findByAssignmentSubjectIdOrderBySubmittedAtDesc(
                                subjectId.value
                        )
                        .map { converter.toDomain(it) }
        }

        override fun findAll(): List<Submission> {
                return jpaRepository.findAll().map { converter.toDomain(it) }
        }

        override fun save(submission: Submission): Submission {
                val entity = converter.toEntity(submission)
                val saved = jpaRepository.save(entity)
                return converter.toDomain(saved)
        }
}
