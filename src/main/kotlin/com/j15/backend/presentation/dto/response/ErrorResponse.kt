package com.j15.backend.presentation.dto.response

// エラーレスポンスDTO
data class ErrorResponse(
        val message: String,
        val status: Int,
        val timestamp: Long = System.currentTimeMillis()
)
