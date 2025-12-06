package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.SectionUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.presentation.dto.response.SectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subjects/{subjectId}/sections")
class SectionController(private val sectionUseCase: SectionUseCase) {

    /** 特定題材の全セクション一覧を取得 フロントエンドが利用可能な全セクション情報を返す */
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

    /** 特定セクションの詳細を取得 */
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
