package com.j15.backend.domain.repository.assignment

import com.j15.backend.domain.model.assignment.AssignmentSubject
import com.j15.backend.domain.model.assignment.AssignmentSubjectId

/** 課題題材リポジトリ（ドメイン層のインターフェース） */
interface AssignmentSubjectRepository {
    fun findById(id: AssignmentSubjectId): AssignmentSubject?
    fun findAll(): List<AssignmentSubject>
    fun save(subject: AssignmentSubject): AssignmentSubject
    fun deleteById(id: AssignmentSubjectId)
    fun existsById(id: AssignmentSubjectId): Boolean
}
