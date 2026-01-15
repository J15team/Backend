package com.j15.backend.presentation.controller.assignment

import com.j15.backend.application.usecase.assignment.AssignmentSectionUseCase
import com.j15.backend.presentation.dto.assignment.AssignmentSectionDetailResponse
import com.j15.backend.presentation.dto.assignment.AssignmentSectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 課題セクション照会コントローラー（公開） */
@RestController
@RequestMapping("/api/assignments/{assignmentSubjectId}/sections")
class AssignmentSectionQueryController(
        private val assignmentSectionUseCase: AssignmentSectionUseCase
) {

    @GetMapping
    fun getAllSections(
            @PathVariable assignmentSubjectId: Long
    ): ResponseEntity<List<AssignmentSectionResponse>> {
        return try {
            val sections = assignmentSectionUseCase.getSectionsBySubject(assignmentSubjectId)
            ResponseEntity.ok(sections.map { AssignmentSectionResponse.from(it) })
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{sectionId}")
    fun getSection(
            @PathVariable assignmentSubjectId: Long,
            @PathVariable sectionId: Int
    ): ResponseEntity<AssignmentSectionDetailResponse> {
        val section =
                assignmentSectionUseCase.getSection(assignmentSubjectId, sectionId)
                        ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(AssignmentSectionDetailResponse.from(section))
    }
}
