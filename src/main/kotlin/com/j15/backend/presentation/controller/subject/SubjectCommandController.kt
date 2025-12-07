package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.SubjectUseCase
import com.j15.backend.presentation.dto.subject.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 題材操作コントローラー 責務: 題材の作成・更新・削除 */
@RestController
@RequestMapping("/api/subjects")
class SubjectCommandController(private val subjectUseCase: SubjectUseCase) {

    @PostMapping
    fun createSubject(@RequestBody request: CreateSubjectRequest): ResponseEntity<SubjectResponse> {
        val subject =
                subjectUseCase.createSubject(
                        subjectId = request.subjectId,
                        title = request.title,
                        description = request.description,
                        maxSections = request.maxSections
                )

        return ResponseEntity.status(HttpStatus.CREATED).body(SubjectResponse.from(subject))
    }

    @PutMapping("/{subjectId}")
    fun updateSubject(
            @PathVariable subjectId: Long,
            @RequestBody request: UpdateSubjectRequest
    ): ResponseEntity<SubjectResponse> {
        return try {
            val subject =
                    subjectUseCase.updateSubject(
                            subjectId = subjectId,
                            title = request.title,
                            description = request.description,
                            maxSections = request.maxSections
                    )
            ResponseEntity.ok(SubjectResponse.from(subject))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{subjectId}")
    fun deleteSubject(@PathVariable subjectId: Long): ResponseEntity<Void> {
        return try {
            subjectUseCase.deleteSubject(subjectId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
