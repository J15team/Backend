package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AdminUseCase
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 管理者専用コントローラ
 * IP制限またはキー検証を通じてのみアクセス可能
 */
@RestController
@RequestMapping("/api/admin")
class AdminController(
    private val adminUseCase: AdminUseCase
) {
    private val logger = LoggerFactory.getLogger(AdminController::class.java)

    /**
     * 管理者アカウントを作成
     * POST /api/admin/users
     * 
     * 必須ヘッダー: X-Admin-Key (管理キー)
     */
    @PostMapping("/users")
    fun createAdminUser(
        @Valid @RequestBody request: AdminUserCreateRequest,
        @RequestHeader("X-Admin-Key") adminKey: String?
    ): ResponseEntity<Any> {
        if (adminKey == null) {
            throw SecurityException("X-Admin-Key header is required")
        }

        val result = adminUseCase.createAdminUser(
            email = request.email,
            username = request.username,
            password = request.password,
            providedKey = adminKey
        )
        
        val response = AdminUserCreateResponse(
            userId = result.user.userId.value.toString(),
            email = result.user.email.value,
            username = result.user.username.value,
            role = result.user.role.name
        )
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
