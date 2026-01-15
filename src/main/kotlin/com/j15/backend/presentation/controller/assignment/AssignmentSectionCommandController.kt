package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.AssignmentSectionUseCase
import com.j15.backend.presentation.dto.assignment.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** 課題セクション操作コントローラー（ADMIN権限必須） */
@RestController
@RequestMapping("/api/assignments/{assignmentSubjectId}/sections")
class AssignmentSectionCommandController(
        private val assignmentSectionUseCase: AssignmentSectionUseCase
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createAssignmentSection(
            @PathVariable assignmentSubjectId: Long,
            @RequestBody request: CreateAssignmentSectionRequest
    ): ResponseEntity<AssignmentSectionResponse> {
        return try {
            val section =
                    assignmentSectionUseCase.createSection(
                            assignmentSubjectId = assignmentSubjectId,
                            sectionId = request.sectionId,
                            title = request.title,
                            description = request.description,
                            hasAssignment = request.hasAssignment,
                            testCases = request.testCases,
                            timeLimit = request.timeLimit,
                            memoryLimit = request.memoryLimit
                    )

            ResponseEntity.status(HttpStatus.CREATED).body(AssignmentSectionResponse.from(section))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAssignmentSection(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int,
            @RequestBody request: UpdateAssignmentSectionRequest
    ): ResponseEntity<AssignmentSectionResponse> {
        return try {
            val section =
                    assignmentSectionUseCase.updateSection(
                            assignmentSubjectId = assignmentSubjectId,
                            sectionId = sectionId,
                            title = request.title,
                            description = request.description,
                            hasAssignment = request.hasAssignment,
                            testCases = request.testCases,
                            timeLimit = request.timeLimit,
                            memoryLimit = request.memoryLimit
                    )
            ResponseEntity.ok(AssignmentSectionResponse.from(section))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @DeleteMapping("/{sectionId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAssignmentSection(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int
    ): ResponseEntity<Void> {
        return try {
            assignmentSectionUseCase.deleteSection(assignmentSubjectId, sectionId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
