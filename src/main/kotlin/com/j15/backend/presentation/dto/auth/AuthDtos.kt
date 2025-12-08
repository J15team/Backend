package com.j15.backend.presentation.dto.auth

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignInResponse(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val username: String
)

data class SignUpResponse(
    val userId: String,
    val email: String,
    val username: String,
    val message: String = "User registered successfully"
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)
