package com.j15.backend.presentation.controller.admin

import com.j15.backend.application.usecase.admin.AdminAssignmentProgressResponse
import com.j15.backend.application.usecase.admin.AdminProgressUseCase
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

/** 管理者向け進捗ダッシュボードコントローラー */
@RestController
@RequestMapping("/api/admin/progress")
class AdminProgressController(private val adminProgressUseCase: AdminProgressUseCase) {

    @GetMapping("/assignments")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAssignmentProgress(): ResponseEntity<AdminAssignmentProgressResponse> {
        val progress = adminProgressUseCase.getAllAssignmentProgress()
        return ResponseEntity.ok(progress)
    }
}
