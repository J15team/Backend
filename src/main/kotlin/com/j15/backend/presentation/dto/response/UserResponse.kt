package com.j15.backend.presentation.dto.response

import java.time.Instant

// ユーザーレスポンスDTO
data class UserResponse(
        val userId: String,
        val username: String,
        val email: String,
        val createdAt: Instant
)
