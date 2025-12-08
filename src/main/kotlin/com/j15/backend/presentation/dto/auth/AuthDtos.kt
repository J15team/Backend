package com.j15.backend.presentation.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignInRequest(
    @field:Email(message = "有効なメールアドレスを入力してください")
    @field:NotBlank(message = "メールアドレスは必須です")
    val email: String,
    
    @field:NotBlank(message = "パスワードは必須です")
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
    @field:Email(message = "有効なメールアドレスを入力してください")
    @field:NotBlank(message = "メールアドレスは必須です")
    val email: String,
    
    @field:Size(min = 8, message = "パスワードは8文字以上である必要があります")
    @field:NotBlank(message = "パスワードは必須です")
    val password: String,
    
    @field:Size(min = 3, max = 50, message = "ユーザー名は3〜50文字である必要があります")
    @field:NotBlank(message = "ユーザー名は必須です")
    val username: String
)

data class SignUpResponse(
    val userId: String,
    val email: String,
    val username: String,
    val message: String = "User registered successfully"
)

data class TokenRefreshRequest(
    @field:NotBlank(message = "リフレッシュトークンは必須です")
    val refreshToken: String
)

data class TokenRefreshResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)
