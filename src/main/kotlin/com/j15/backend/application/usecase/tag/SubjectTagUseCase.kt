package com.j15.backend.application.usecase.tag

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.repository.SubjectRepository
import com.j15.backend.domain.repository.SubjectTagRepository
import com.j15.backend.domain.repository.TagRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** 題材-タグ関連管理ユースケース */
@Service
@Transactional
class SubjectTagUseCase(
        private val tagRepository: TagRepository,
        private val subjectRepository: SubjectRepository,
        private val subjectTagRepository: SubjectTagRepository
) {

    /** 題材にタグを付与 */
    fun addTagToSubject(subjectId: Long, tagId: Long) {
        val sId = SubjectId(subjectId)
        val tId = TagId(tagId)

        if (!subjectRepository.existsById(sId)) {
            throw IllegalArgumentException("題材が見つかりません: $subjectId")
        }
        if (!tagRepository.existsById(tId)) {
            throw IllegalArgumentException("タグが見つかりません: $tagId")
        }

        subjectTagRepository.addTagToSubject(sId, tId)
    }

    /** 題材からタグを削除 */
    fun removeTagFromSubject(subjectId: Long, tagId: Long) {
        val sId = SubjectId(subjectId)
        val tId = TagId(tagId)
        subjectTagRepository.removeTagFromSubject(sId, tId)
    }

    /** 題材のタグを取得 */
    @Transactional(readOnly = true)
    fun getTagsForSubject(subjectId: Long): List<Tag> {
        val sId = SubjectId(subjectId)
        if (!subjectRepository.existsById(sId)) {
            throw IllegalArgumentException("題材が見つかりません: $subjectId")
        }
        return subjectTagRepository.findTagsBySubjectId(sId)
    }

    /** タグで題材を絞り込み（AND条件） */
    @Transactional(readOnly = true)
    fun getSubjectsByTags(tagIds: List<Long>): List<Subject> {
        if (tagIds.isEmpty()) {
            return subjectRepository.findAll()
        }
        val tIds = tagIds.map { TagId(it) }
        val subjectIds = subjectTagRepository.findSubjectIdsByTagIds(tIds)
        return subjectIds.mapNotNull { subjectRepository.findById(it) }.sortedBy {
            it.subjectId.value
        }
    }
}
