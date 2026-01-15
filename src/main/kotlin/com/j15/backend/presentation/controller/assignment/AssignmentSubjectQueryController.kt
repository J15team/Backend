package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.AssignmentSubjectUseCase
import com.j15.backend.presentation.dto.assignment.AssignmentSubjectResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 課題題材照会コントローラー（公開） */
@RestController
@RequestMapping("/api/assignments")
class AssignmentSubjectQueryController(
        private val assignmentSubjectUseCase: AssignmentSubjectUseCase
) {

    @GetMapping("/{assignmentSubjectId}")
    fun getAssignmentSubject(
            @PathVariable assignmentSubjectId: Long
    ): ResponseEntity<AssignmentSubjectResponse> {
        val subject =
                assignmentSubjectUseCase.getSubject(assignmentSubjectId)
                        ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(AssignmentSubjectResponse.from(subject))
    }

    @GetMapping
    fun getAllAssignmentSubjects(): ResponseEntity<List<AssignmentSubjectResponse>> {
        val subjects = assignmentSubjectUseCase.getAllSubjects()
        return ResponseEntity.ok(subjects.map { AssignmentSubjectResponse.from(it) })
    }
}
