package com.j15.backend.application.command

/**
 * サインインコマンド
 */
data class SignInCommand(
    val email: String,
    val password: String
)

/**
 * サインアップコマンド
 */
data class SignUpCommand(
    val email: String,
    val password: String,
    val username: String
)

/**
 * トークンリフレッシュコマンド
 */
data class RefreshTokenCommand(
    val refreshToken: String
)
