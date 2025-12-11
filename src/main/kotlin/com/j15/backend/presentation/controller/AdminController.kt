package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AdminUseCase
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
import com.j15.backend.presentation.dto.response.ErrorResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
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
        bindingResult: BindingResult,
        @RequestHeader("X-Admin-Key") adminKey: String?
    ): ResponseEntity<Any> {
        // 1. まず認証チェック (401)
        if (adminKey == null) {
            throw SecurityException("X-Admin-Key header is required")
        }
        adminUseCase.validateAdminKey(adminKey)

        // 2. 次にバリデーションチェック (400)
        if (bindingResult.hasErrors()) {
            val errors = bindingResult.allErrors.joinToString(", ") { error ->
                val fieldName = (error as? FieldError)?.field ?: "field"
                val errorMessage = error.defaultMessage ?: "invalid"
                "$fieldName: $errorMessage"
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse(message = errors, status = HttpStatus.BAD_REQUEST.value()))
        }

        // 3. 正常系の処理
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
