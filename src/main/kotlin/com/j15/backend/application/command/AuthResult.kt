package com.j15.backend.application.command

/**
 * サインイン結果
 */
data class SignInResult(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String
)

/**
 * サインアップ結果
 */
data class SignUpResult(
    val userId: String,
    val email: String,
    val username: String
)

/**
 * トークンリフレッシュ結果
 */
data class RefreshTokenResult(
    val accessToken: String
)
