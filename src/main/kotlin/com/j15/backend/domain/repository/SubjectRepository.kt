package com.j15.backend.domain.repository

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId

// 題材リポジトリ（ドメイン層のインターフェース）
interface SubjectRepository {
    fun findById(subjectId: SubjectId): Subject?
    fun findAll(): List<Subject>
    fun save(subject: Subject): Subject
    fun deleteById(subjectId: SubjectId)
    fun existsById(subjectId: SubjectId): Boolean
}
