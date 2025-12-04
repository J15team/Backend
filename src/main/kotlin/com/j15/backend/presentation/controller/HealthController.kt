package com.j15.backend.presentation.controller

import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/** ヘルスチェックコントローラー（プレゼンテーション層） */
@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
                HealthResponse(
                        status = "UP",
                        message = "Backend service is running",
                        timestamp = LocalDateTime.now().toString()
                )
        )
    }
}

data class HealthResponse(val status: String, val message: String, val timestamp: String)
