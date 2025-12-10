package com.j15.backend.presentation.dto.response

data class SignInResponse(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class SignUpResponse(
    val userId: String,
    val email: String,
    val username: String,
    val message: String = "User registered successfully"
)

data class TokenRefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)
