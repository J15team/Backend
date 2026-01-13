package com.j15.backend.presentation.controller.tag

import com.j15.backend.application.usecase.tag.TagUseCase
import com.j15.backend.domain.model.tag.TagType
import com.j15.backend.presentation.dto.tag.CreateTagRequest
import com.j15.backend.presentation.dto.tag.TagExistsResponse
import com.j15.backend.presentation.dto.tag.TagResponse
import com.j15.backend.presentation.dto.tag.TagWithSubjectsResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** タグ管理コントローラー */
@RestController
@RequestMapping("/api/tags")
class TagController(private val tagUseCase: TagUseCase) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createTag(@RequestBody request: CreateTagRequest): ResponseEntity<TagResponse> {
        return try {
            val type = request.type?.let { TagType.valueOf(it) } ?: TagType.NORMAL
            val tag = tagUseCase.createTag(request.name, type)
            ResponseEntity.status(HttpStatus.CREATED).body(TagResponse.from(tag))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping
    fun getAllTags(
            @RequestParam(required = false) search: String?,
            @RequestParam(required = false, defaultValue = "false") includeSubjects: Boolean
    ): ResponseEntity<*> {
        if (includeSubjects && search.isNullOrBlank()) {
            val tagsWithSubjects = tagUseCase.getAllTagsWithSubjects()
            return ResponseEntity.ok(
                    tagsWithSubjects.map { TagWithSubjectsResponse.from(it.tag, it.subjectIds) }
            )
        }
        val tags =
                if (search.isNullOrBlank()) {
                    tagUseCase.getAllTags()
                } else {
                    tagUseCase.searchTags(search)
                }
        return ResponseEntity.ok(tags.map { TagResponse.from(it) })
    }

    @GetMapping("/{tagId}")
    fun getTag(@PathVariable tagId: Long): ResponseEntity<TagResponse> {
        val tag = tagUseCase.getTag(tagId) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TagResponse.from(tag))
    }

    @GetMapping("/name/{name}")
    fun getTagByName(@PathVariable name: String): ResponseEntity<TagResponse> {
        val tag = tagUseCase.getTagByName(name) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(TagResponse.from(tag))
    }

    @GetMapping("/exists")
    fun checkTagExists(@RequestParam name: String): ResponseEntity<TagExistsResponse> {
        val exists = tagUseCase.existsByName(name)
        val tag = if (exists) tagUseCase.getTagByName(name)?.let { TagResponse.from(it) } else null
        return ResponseEntity.ok(TagExistsResponse(exists, tag))
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteTag(@PathVariable tagId: Long): ResponseEntity<Void> {
        return try {
            tagUseCase.deleteTag(tagId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
