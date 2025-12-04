package com.j15.backend.controller

import java.time.LocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class HealthController {

    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<HealthResponse> {
        return ResponseEntity.ok(
                HealthResponse(
                        status = "200",
                        message = "Backend service is running",
                        timestamp = LocalDateTime.now().toString()
                )
        )
    }
}

data class HealthResponse(val status: String, val message: String, val timestamp: String)
