package com.j15.backend.domain.model.auth

// 認証トークンのペア
data class AuthTokens(
    val accessToken: AccessToken,
    val refreshToken: RefreshToken
)
