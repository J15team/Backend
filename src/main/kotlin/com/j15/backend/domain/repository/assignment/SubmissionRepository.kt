package com.j15.backend.domain.repository.assignment

import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId
import com.j15.backend.domain.model.assignment.Submission
import com.j15.backend.domain.model.assignment.SubmissionId
import com.j15.backend.domain.model.user.UserId

/** 提出リポジトリ（ドメイン層のインターフェース） INSERT only（更新・削除メソッドなし） */
interface SubmissionRepository {
    fun findById(id: SubmissionId): Submission?

    fun findByUserAndSection(
            userId: UserId,
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission>

    fun findByUserAndSubject(userId: UserId, subjectId: AssignmentSubjectId): List<Submission>

    fun findBySection(
            subjectId: AssignmentSubjectId,
            sectionId: AssignmentSectionId
    ): List<Submission>

    fun findBySubject(subjectId: AssignmentSubjectId): List<Submission>

    fun findAll(): List<Submission>

    fun save(submission: Submission): Submission
}
