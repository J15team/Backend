package com.j15.backend.presentation.dto.response

// ログイン成功レスポンスDTO
data class LoginResponse(
        val userId: String,
        val username: String,
        val email: String,
        val message: String = "ログインに成功しました"
)
