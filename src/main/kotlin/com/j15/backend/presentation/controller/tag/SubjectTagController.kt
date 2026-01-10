package com.j15.backend.presentation.controller.tag

import com.j15.backend.application.usecase.tag.SubjectTagUseCase
import com.j15.backend.presentation.dto.tag.TagResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** 題材-タグ関連コントローラー */
@RestController
@RequestMapping("/api/subjects/{subjectId}/tags")
class SubjectTagController(private val subjectTagUseCase: SubjectTagUseCase) {

    @PostMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun addTagToSubject(
            @PathVariable subjectId: Long,
            @PathVariable tagId: Long
    ): ResponseEntity<Void> {
        return try {
            subjectTagUseCase.addTagToSubject(subjectId, tagId)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun removeTagFromSubject(
            @PathVariable subjectId: Long,
            @PathVariable tagId: Long
    ): ResponseEntity<Void> {
        subjectTagUseCase.removeTagFromSubject(subjectId, tagId)
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
