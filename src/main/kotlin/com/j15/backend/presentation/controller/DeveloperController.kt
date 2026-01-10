package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.DeveloperUseCase
import com.j15.backend.presentation.dto.response.DeveloperVerifyResponse
import com.j15.backend.presentation.dto.response.EndpointsListResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/** 開発者専用コントローラ X-Dev-Keyヘッダーで認証 */
@RestController
@RequestMapping("/api/dev")
class DeveloperController(private val developerUseCase: DeveloperUseCase) {
    private val logger = LoggerFactory.getLogger(DeveloperController::class.java)

    /**
     * 開発者確認 GET /api/dev/verify
     *
     * X-Dev-Keyが有効な場合は { isDeveloper: true } 無効または未設定の場合は { isDeveloper: false }
     */
    @GetMapping("/verify")
    fun verifyDeveloper(
            @RequestHeader("X-Dev-Key", required = false) devKey: String?
    ): ResponseEntity<DeveloperVerifyResponse> {
        // 開発者機能が無効の場合
        if (!developerUseCase.isDevModeEnabled()) {
            return ResponseEntity.ok(DeveloperVerifyResponse(isDeveloper = false))
        }

        // キーが未設定または無効の場合
        if (devKey == null || !developerUseCase.validateDevKey(devKey)) {
            return ResponseEntity.ok(DeveloperVerifyResponse(isDeveloper = false))
        }

        return ResponseEntity.ok(DeveloperVerifyResponse(isDeveloper = true))
    }

    /**
     * エンドポイント一覧取得 GET /api/dev/endpoints
     *
     * 必須ヘッダー: X-Dev-Key
     */
    @GetMapping("/endpoints")
    fun getEndpoints(
            @RequestHeader("X-Dev-Key", required = false) devKey: String?
    ): ResponseEntity<Any> {
        // 開発者機能が無効の場合
        if (!developerUseCase.isDevModeEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(mapOf("error" to "開発者機能は無効です"))
        }

        // キーの検証
        if (devKey == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "X-Dev-Key header is required"))
        }

        if (!developerUseCase.validateDevKey(devKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(mapOf("error" to "認証に失敗しました"))
        }

        val endpoints = developerUseCase.getAllEndpoints()
        return ResponseEntity.ok(
                EndpointsListResponse(endpoints = endpoints, count = endpoints.size)
        )
    }

    /**
     * ユーザー削除 DELETE /api/dev/users/{userId}
     *
     * 必須ヘッダー: X-Dev-Key 注意: ROLE_DEVELOPERは削除不可
     */
    @DeleteMapping("/users/{userId}")
    fun deleteUser(
            @PathVariable userId: String,
            @RequestHeader("X-Dev-Key", required = false) devKey: String?
    ): ResponseEntity<Any> {
        return try {
            // 開発者機能が無効の場合
            if (!developerUseCase.isDevModeEnabled()) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(mapOf("error" to "開発者機能は無効です"))
            }

            // キーの検証
            if (devKey == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf("error" to "X-Dev-Key header is required"))
            }

            if (!developerUseCase.validateDevKey(devKey)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(mapOf("error" to "認証に失敗しました"))
            }

            developerUseCase.deleteUser(userId)
            ResponseEntity.noContent().build()
        } catch (e: SecurityException) {
            logger.warn("User deletion blocked: ${e.message}")
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("error" to e.message))
        } catch (e: IllegalArgumentException) {
            logger.warn("User deletion failed: ${e.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to e.message))
        } catch (e: Exception) {
            logger.error("Unexpected error during user deletion: ${e.message}", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "ユーザーの削除中にエラーが発生しました"))
        }
    }
}
