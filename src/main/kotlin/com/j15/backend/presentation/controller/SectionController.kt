package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.SectionUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.presentation.dto.response.SectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/sections")
class SectionController(private val sectionUseCase: SectionUseCase) {

    /** 全セクション一覧を取得 フロントエンドが利用可能な全セクション情報を返す */
    @GetMapping
    fun getAllSections(): ResponseEntity<List<SectionResponse>> {
        val sections = sectionUseCase.getAllSections()
        val response = sections.map { SectionResponse.from(it) }
        return ResponseEntity.ok(response)
    }

    /** 特定セクションの詳細を取得 */
    @GetMapping("/{sectionId}")
    fun getSectionById(@PathVariable sectionId: Int): ResponseEntity<SectionResponse> {
        val section = sectionUseCase.getSectionById(SectionId(sectionId))
        return if (section != null) {
            ResponseEntity.ok(SectionResponse.from(section))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
