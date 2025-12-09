package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.subject.SectionUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.presentation.dto.response.SectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** セクション照会コントローラー 責務: セクション情報の取得（一覧・詳細） */
@RestController
@RequestMapping("/api/subjects/{subjectId}/sections")
class SectionQueryController(private val sectionUseCase: SectionUseCase) {

    @GetMapping
    fun getAllSections(@PathVariable subjectId: Long): ResponseEntity<List<SectionResponse>> {
        return try {
            val sections = sectionUseCase.getAllSections(SubjectId(subjectId))
            val response = sections.map { SectionResponse.from(it) }
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    @GetMapping("/{sectionId}")
    fun getSectionById(
            @PathVariable subjectId: Long,
            @PathVariable sectionId: Int
    ): ResponseEntity<SectionResponse> {
        val section = sectionUseCase.getSectionById(SubjectId(subjectId), SectionId(sectionId))
        return if (section != null) {
            ResponseEntity.ok(SectionResponse.from(section))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
