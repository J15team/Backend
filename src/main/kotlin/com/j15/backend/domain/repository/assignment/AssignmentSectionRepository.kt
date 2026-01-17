package com.j15.backend.domain.repository.assignment

import com.j15.backend.domain.model.assignment.AssignmentSection
import com.j15.backend.domain.model.assignment.AssignmentSectionId
import com.j15.backend.domain.model.assignment.AssignmentSubjectId

/** 課題セクションリポジトリ（ドメイン層のインターフェース） */
interface AssignmentSectionRepository {
    fun findById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId): AssignmentSection?
    fun findBySubjectId(subjectId: AssignmentSubjectId): List<AssignmentSection>
    fun save(section: AssignmentSection): AssignmentSection
    fun deleteById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId)
    fun deleteAllBySubjectId(subjectId: AssignmentSubjectId)
    fun existsById(subjectId: AssignmentSubjectId, sectionId: AssignmentSectionId): Boolean
}
