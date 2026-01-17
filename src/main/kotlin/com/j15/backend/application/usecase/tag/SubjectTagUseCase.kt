package com.j15.backend.application.usecase.tag

import com.j15.backend.domain.model.subject.Subject
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.repository.subject.SubjectRepository
import com.j15.backend.domain.repository.tag.SubjectTagRepository
import com.j15.backend.domain.repository.tag.TagRepository
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

    /** 題材にタグを付与（タグ名で指定） */
    fun addTagToSubject(subjectId: Long, tagName: String) {
        val sId = SubjectId(subjectId)
        val tName = TagName(tagName)

        if (!subjectRepository.existsById(sId)) {
            throw IllegalArgumentException("題材が見つかりません: $subjectId")
        }
        val tag =
                tagRepository.findByName(tName)
                        ?: throw IllegalArgumentException("タグが見つかりません: $tagName")

        subjectTagRepository.addTagToSubject(sId, tag.id)
    }

    /** 題材からタグを削除（タグ名で指定） */
    fun removeTagFromSubject(subjectId: Long, tagName: String) {
        val sId = SubjectId(subjectId)
        val tName = TagName(tagName)
        val tag = tagRepository.findByName(tName) ?: return // 存在しないタグは無視
        subjectTagRepository.removeTagFromSubject(sId, tag.id)
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

    /** タグ名で題材を絞り込み（AND条件） */
    @Transactional(readOnly = true)
    fun getSubjectsByTagNames(tagNames: List<String>): List<Subject> {
        if (tagNames.isEmpty()) {
            return subjectRepository.findAll()
        }
        val tagIds =
                tagNames.mapNotNull { name ->
                    try {
                        tagRepository.findByName(TagName(name))?.id
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
        if (tagIds.isEmpty()) {
            return emptyList()
        }
        val subjectIds = subjectTagRepository.findSubjectIdsByTagIds(tagIds)
        return subjectIds.mapNotNull { subjectRepository.findById(it) }.sortedBy {
            it.subjectId.value
        }
    }
}
