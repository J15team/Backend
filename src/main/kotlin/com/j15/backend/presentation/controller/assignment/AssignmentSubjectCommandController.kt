package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.AssignmentSubjectUseCase
import com.j15.backend.presentation.dto.assignment.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** 課題題材操作コントローラー（ADMIN権限必須） */
@RestController
@RequestMapping("/api/assignments")
class AssignmentSubjectCommandController(
        private val assignmentSubjectUseCase: AssignmentSubjectUseCase
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createAssignmentSubject(
            @RequestBody request: CreateAssignmentSubjectRequest
    ): ResponseEntity<AssignmentSubjectResponse> {
        val subject =
                assignmentSubjectUseCase.createSubject(
                        assignmentSubjectId = request.assignmentSubjectId,
                        title = request.title,
                        description = request.description,
                        maxSections = request.maxSections,
                        weight = request.weight
                )

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AssignmentSubjectResponse.from(subject))
    }

    @PutMapping("/{assignmentSubjectId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAssignmentSubject(
            @PathVariable assignmentSubjectId: Long,
            @RequestBody request: UpdateAssignmentSubjectRequest
    ): ResponseEntity<AssignmentSubjectResponse> {
        return try {
            val subject =
                    assignmentSubjectUseCase.updateSubject(
                            assignmentSubjectId = assignmentSubjectId,
                            title = request.title,
                            description = request.description,
                            maxSections = request.maxSections,
                            weight = request.weight
                    )
            ResponseEntity.ok(AssignmentSubjectResponse.from(subject))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{assignmentSubjectId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAssignmentSubject(@PathVariable assignmentSubjectId: Long): ResponseEntity<Void> {
        return try {
            assignmentSubjectUseCase.deleteSubject(assignmentSubjectId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
