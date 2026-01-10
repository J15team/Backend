package com.j15.backend.presentation.controller.tag

import com.j15.backend.application.usecase.tag.SubjectTagUseCase
import com.j15.backend.presentation.dto.tag.TagResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** タグ追加・削除リクエストDTO */
data class TagNameRequest(val tagName: String)

/** 題材-タグ関連コントローラー */
@RestController
@RequestMapping("/api/subjects/{subjectId}/tags")
class SubjectTagController(private val subjectTagUseCase: SubjectTagUseCase) {

    /** タグを題材に追加（リクエストボディでタグ名を指定） */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun addTagToSubject(
            @PathVariable subjectId: Long,
            @RequestBody request: TagNameRequest
    ): ResponseEntity<Void> {
        return try {
            subjectTagUseCase.addTagToSubject(subjectId, request.tagName)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /** タグを題材から削除（リクエストボディでタグ名を指定） */
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun removeTagFromSubject(
            @PathVariable subjectId: Long,
            @RequestBody request: TagNameRequest
    ): ResponseEntity<Void> {
        subjectTagUseCase.removeTagFromSubject(subjectId, request.tagName)
        return ResponseEntity.noContent().build()
    }

    @GetMapping
    fun getTagsForSubject(@PathVariable subjectId: Long): ResponseEntity<List<TagResponse>> {
        return try {
            val tags = subjectTagUseCase.getTagsForSubject(subjectId)
            ResponseEntity.ok(tags.map { TagResponse.from(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
