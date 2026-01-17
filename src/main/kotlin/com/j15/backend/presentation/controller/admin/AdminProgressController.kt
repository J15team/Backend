package com.j15.backend.presentation.controller.admin

import com.j15.backend.application.usecase.admin.AdminAssignmentProgressResponse
import com.j15.backend.application.usecase.admin.AdminProgressUseCase
import com.j15.backend.application.usecase.admin.AdminSubjectProgressResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** 管理者向け進捗ダッシュボードコントローラー */
@RestController
@RequestMapping("/api/admin/progress")
class AdminProgressController(private val adminProgressUseCase: AdminProgressUseCase) {

    /** 課題題材の進捗一覧 */
    @GetMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAssignmentProgress(): ResponseEntity<AdminAssignmentProgressResponse> {
        val progress = adminProgressUseCase.getAllAssignmentProgress()
        return ResponseEntity.ok(progress)
    }

    /** 通常題材の進捗一覧 */
    @GetMapping("/subjects")
    @PreAuthorize("hasRole('ADMIN')")
    fun getSubjectProgress(): ResponseEntity<AdminSubjectProgressResponse> {
        val progress = adminProgressUseCase.getAllSubjectProgress()
        return ResponseEntity.ok(progress)
    }
}
