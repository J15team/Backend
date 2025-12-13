package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AdminUseCase
import com.j15.backend.presentation.dto.request.AdminUserCreateRequest
import com.j15.backend.presentation.dto.request.AdminUserUpdateRequest
import com.j15.backend.presentation.dto.response.AdminUserCreateResponse
import com.j15.backend.presentation.dto.response.AdminUserResponse
import com.j15.backend.presentation.dto.response.AdminUserUpdateResponse
import com.j15.backend.presentation.dto.response.AdminUsersListResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

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
        } catch (e: SecurityException) {
            logger.warn("Admin user creation failed due to invalid key", e)
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "認証に失敗しました"))
        } catch (e: IllegalArgumentException) {
            logger.warn("Admin user creation failed: ${e.message}", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during admin user creation: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "管理者ユーザーの作成中にエラーが発生しました"))
        }
    }

    /**
     * 管理者ユーザー一覧を取得
     * GET /api/admin/users
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllAdminUsers(): ResponseEntity<AdminUsersListResponse> {
        return try {
            val admins = adminUseCase.getAllAdminUsers()
            val adminResponses = admins.map { user ->
                AdminUserResponse(
                    userId = user.userId.value.toString(),
                    email = user.email.value,
                    username = user.username.value,
                    role = user.role.name
                )
            }
            ResponseEntity.ok(AdminUsersListResponse(admins = adminResponses))
        } catch (e: Exception) {
            logger.error("Error fetching admin users: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(AdminUsersListResponse(admins = emptyList()))
        }
    }

    /**
     * 管理者ユーザーの詳細を取得
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAdminUser(
        @PathVariable userId: String
    ): ResponseEntity<Any> {
        return try {
            val user = adminUseCase.getAdminUser(userId)
            if (user == null) {
                return ResponseEntity.notFound().build()
            }
            val response = AdminUserResponse(
                userId = user.userId.value.toString(),
                email = user.email.value,
                username = user.username.value,
                role = user.role.name
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Invalid user ID: ${e.message}", e)
            ResponseEntity.badRequest().body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Error fetching admin user: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "ユーザー情報の取得中にエラーが発生しました"))
        }
    }

    /**
     * 管理者ユーザーを更新
     * PUT /api/admin/users/{userId}
     */
    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAdminUser(
        @PathVariable userId: String,
        @RequestBody request: AdminUserUpdateRequest
    ): ResponseEntity<Any> {
        return try {
            val result = adminUseCase.updateAdminUser(
                userId = userId,
                email = request.email,
                username = request.username,
                password = request.password
            )
            
            val response = AdminUserUpdateResponse(
                userId = result.user.userId.value.toString(),
                email = result.user.email.value,
                username = result.user.username.value,
                role = result.user.role.name
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            logger.warn("Admin user update failed: ${e.message}", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during admin user update: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "管理者ユーザーの更新中にエラーが発生しました"))
        }
    }

    /**
     * 管理者ユーザーを削除
     * DELETE /api/admin/users/{userId}
     */
    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAdminUser(
        @PathVariable userId: String
    ): ResponseEntity<Any> {
        return try {
            adminUseCase.deleteAdminUser(userId)
            ResponseEntity.noContent().build()
        } catch (e: IllegalArgumentException) {
            logger.warn("Admin user deletion failed: ${e.message}", e)
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during admin user deletion: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "管理者ユーザーの削除中にエラーが発生しました"))
        }
    }
}

