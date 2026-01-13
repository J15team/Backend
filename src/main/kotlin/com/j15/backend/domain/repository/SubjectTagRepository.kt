package com.j15.backend.domain.repository

import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId

/** 題材-タグ関連リポジトリ（ドメイン層のインターフェース） */
interface SubjectTagRepository {
    fun findTagsBySubjectId(subjectId: SubjectId): List<Tag>
    fun findSubjectIdsByTagId(tagId: TagId): List<SubjectId>
    fun findSubjectIdsByTagIds(tagIds: List<TagId>): List<SubjectId>
    fun findAllAssociations(): Map<TagId, List<SubjectId>>
    fun addTagToSubject(subjectId: SubjectId, tagId: TagId)
    fun removeTagFromSubject(subjectId: SubjectId, tagId: TagId)
    fun removeAllTagsFromSubject(subjectId: SubjectId)
    fun removeTagFromAllSubjects(tagId: TagId)
    fun existsAssociation(subjectId: SubjectId, tagId: TagId): Boolean
}
