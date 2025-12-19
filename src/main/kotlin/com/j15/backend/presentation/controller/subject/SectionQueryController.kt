package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.subject.ImageUseCase
import com.j15.backend.application.usecase.subject.SectionUseCase
import com.j15.backend.domain.model.section.SectionId
import com.j15.backend.domain.model.subject.SubjectId
import com.j15.backend.presentation.dto.response.ImageResponse
import com.j15.backend.presentation.dto.response.SectionResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** セクション照会コントローラー 責務: セクション情報の取得（一覧・詳細） */
@RestController
@RequestMapping("/api/subjects/{subjectId}/sections")
class SectionQueryController(
        private val sectionUseCase: SectionUseCase,
        private val imageUseCase: ImageUseCase
) {

    @GetMapping
    fun getAllSections(@PathVariable subjectId: Long): ResponseEntity<List<SectionResponse>> {
        return try {
            val sections = sectionUseCase.getAllSections(SubjectId(subjectId))
            val response = sections.map { section ->
                // セクションごとに画像を取得
                val images = imageUseCase.getImagesBySectionId(
                    section.subjectId,
                    section.sectionId
                ).map { ImageResponse.from(it) }

                SectionResponse.from(section, images)
            }
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
            // 画像リストを取得
            val images = imageUseCase.getImagesBySectionId(
                section.subjectId,
                section.sectionId
            ).map { ImageResponse.from(it) }

            ResponseEntity.ok(SectionResponse.from(section, images))
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
