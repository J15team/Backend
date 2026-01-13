package com.j15.backend.application.usecase.tag

import com.j15.backend.domain.model.tag.Tag
import com.j15.backend.domain.model.tag.TagId
import com.j15.backend.domain.model.tag.TagName
import com.j15.backend.domain.model.tag.TagType
import com.j15.backend.domain.repository.SubjectTagRepository
import com.j15.backend.domain.repository.TagRepository
import java.time.Instant
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** タグと紐づき題材IDのペア */
data class TagWithSubjectIds(val tag: Tag, val subjectIds: List<Long>)

/** タグ管理ユースケース */
@Service
@Transactional
class TagUseCase(
        private val tagRepository: TagRepository,
        private val subjectTagRepository: SubjectTagRepository
) {

    /** タグを作成 */
    fun createTag(name: String, type: TagType = TagType.NORMAL): Tag {
        val tagName = TagName(name)

        if (tagRepository.existsByName(tagName)) {
            throw IllegalArgumentException("同名のタグが既に存在します: $name")
        }

        val tag =
                Tag(
                        id = TagId(0L), // 自動生成されるため仮のID
                        name = tagName,
                        type = type,
                        createdAt = Instant.now()
                )

        return tagRepository.save(tag)
    }

    /** タグを取得 */
    @Transactional(readOnly = true)
    fun getTag(tagId: Long): Tag? {
        return tagRepository.findById(TagId(tagId))
    }

    /** タグを名前で取得 */
    @Transactional(readOnly = true)
    fun getTagByName(name: String): Tag? {
        return tagRepository.findByName(TagName(name))
    }

    /** タグの存在確認 */
    @Transactional(readOnly = true)
    fun existsByName(name: String): Boolean {
        return try {
            tagRepository.existsByName(TagName(name))
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    /** 全タグを取得（名前順） */
    @Transactional(readOnly = true)
    fun getAllTags(): List<Tag> {
        return tagRepository.findAll()
    }

    /** タグを検索 */
    @Transactional(readOnly = true)
    fun searchTags(query: String): List<Tag> {
        return tagRepository.searchByName(query)
    }

    /** タグをタイプで取得 */
    @Transactional(readOnly = true)
    fun getTagsByType(type: TagType): List<Tag> {
        return tagRepository.findByType(type)
    }

    /** 全タグを紐づき題材ID付きで取得（N+1回避版） */
    @Transactional(readOnly = true)
    fun getAllTagsWithSubjects(): List<TagWithSubjectIds> {
        val tags = tagRepository.findAll()
        val associations = subjectTagRepository.findAllAssociations()
        return tags.map { tag ->
            val subjectIds = associations[tag.id]?.map { it.value } ?: emptyList()
            TagWithSubjectIds(tag, subjectIds)
        }
    }

    /** タグを削除 */
    fun deleteTag(tagId: Long) {
        val id = TagId(tagId)
        if (!tagRepository.existsById(id)) {
            throw IllegalArgumentException("タグが見つかりません: $tagId")
        }
        // 関連する題材-タグ関連を削除
        subjectTagRepository.removeTagFromAllSubjects(id)
        // タグを削除
        tagRepository.deleteById(id)
    }
}
