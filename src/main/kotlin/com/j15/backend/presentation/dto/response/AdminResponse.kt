package com.j15.backend.presentation.dto.response

data class AdminUserCreateResponse(
    val userId: String,
    val email: String,
    val username: String,
    val role: String,
    val message: String = "Admin user created successfully"
)
