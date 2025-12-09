package com.j15.backend.presentation.controller

import com.j15.backend.application.service.AdminService
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
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
    private val adminService: AdminService
) {

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

            val response = adminService.createAdminUser(request, adminKey)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Failed to create admin user"))
        }
    }
}
