package com.j15.backend.presentation.dto.request

data class AdminUserCreateRequest(
    val email: String,
    val password: String,
    val username: String
)
