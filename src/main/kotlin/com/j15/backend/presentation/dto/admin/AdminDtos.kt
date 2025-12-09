package com.j15.backend.presentation.dto.admin

data class AdminUserCreateRequest(
    val email: String,
    val password: String,
    val username: String
)

data class AdminUserCreateResponse(
    val userId: String,
    val email: String,
    val username: String,
    val role: String,
    val message: String = "Admin user created successfully"
)
