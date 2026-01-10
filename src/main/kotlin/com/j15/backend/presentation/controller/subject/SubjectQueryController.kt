package com.j15.backend.presentation.controller.subject

import com.j15.backend.application.usecase.subject.SubjectUseCase
import com.j15.backend.application.usecase.tag.SubjectTagUseCase
import com.j15.backend.presentation.dto.subject.SubjectResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 題材照会コントローラー 責務: 題材の取得（一覧・詳細） */
@RestController
@RequestMapping("/api/subjects")
class SubjectQueryController(
        private val subjectUseCase: SubjectUseCase,
        private val subjectTagUseCase: SubjectTagUseCase
) {

    @GetMapping("/{subjectId}")
    fun getSubject(@PathVariable subjectId: Long): ResponseEntity<SubjectResponse> {
        val subject =
                subjectUseCase.getSubject(subjectId) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(SubjectResponse.from(subject))
    }

    @GetMapping
    fun getAllSubjects(
            @RequestParam(required = false) tags: String?
    ): ResponseEntity<List<SubjectResponse>> {
        val subjects =
                if (tags.isNullOrBlank()) {
                    subjectUseCase.getAllSubjects()
                } else {
                    val tagNames = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    subjectTagUseCase.getSubjectsByTagNames(tagNames)
                }
        return ResponseEntity.ok(subjects.map { SubjectResponse.from(it) })
    }
}
