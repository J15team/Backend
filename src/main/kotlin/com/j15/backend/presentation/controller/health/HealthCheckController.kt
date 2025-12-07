package com.j15.backend.presentation.controller.health

import org.springframework.web.bind.annotation.*

/** ヘルスチェックコントローラー 責務: アプリケーションの健全性確認 */
@RestController
@RequestMapping("/api")
class HealthCheckController {

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP")
    }
}
