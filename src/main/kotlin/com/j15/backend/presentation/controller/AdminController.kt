package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AdminUseCase
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
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
        @RequestBody request: AdminUserCreateRequest,
        @RequestHeader("X-Admin-Key") adminKey: String?
    ): ResponseEntity<Any> {
        return try {
            if (adminKey == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "X-Admin-Key header is required"))
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
            
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Admin user creation failed", e)
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to "管理者ユーザーの作成に失敗しました"))
        } catch (e: Exception) {
            logger.error("Unexpected error during admin user creation", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "管理者ユーザーの作成中にエラーが発生しました"))
        }
    }
}
