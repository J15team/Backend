package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.SubjectUseCase
import com.j15.backend.presentation.dto.subject.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 題材管理コントローラー 題材（学習プロジェクト）のCRUD APIを提供 */
@RestController
@RequestMapping("/api/subjects")
class SubjectController(private val subjectUseCase: SubjectUseCase) {

    /** 題材を作成 POST /api/subjects */
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

    /** 題材を更新 PUT /api/subjects/{subjectId} */
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

    /** 題材を取得 GET /api/subjects/{subjectId} */
    @GetMapping("/{subjectId}")
    fun getSubject(@PathVariable subjectId: Long): ResponseEntity<SubjectResponse> {
        val subject =
                subjectUseCase.getSubject(subjectId) ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(SubjectResponse.from(subject))
    }

    /** 全題材を取得 GET /api/subjects */
    @GetMapping
    fun getAllSubjects(): ResponseEntity<List<SubjectResponse>> {
        val subjects = subjectUseCase.getAllSubjects().map { SubjectResponse.from(it) }

        return ResponseEntity.ok(subjects)
    }

    /** 題材を削除 DELETE /api/subjects/{subjectId} */
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
