package com.j15.backend.presentation.dto.response

// ログイン成功レスポンスDTO
data class LoginResponse(
        val accessToken: String,
        val refreshToken: String,
        val user: UserInfo,
        val message: String = "ログインに成功しました"
) {
    data class UserInfo(
        val id: String,
        val username: String,
        val email: String
    )
}
