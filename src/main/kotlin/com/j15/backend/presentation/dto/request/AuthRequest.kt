package com.j15.backend.presentation.dto.request

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String
)

data class TokenRefreshRequest(
    val refreshToken: String
)
