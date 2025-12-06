package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

// ログインリクエストDTO
data class LoginRequest(
        @field:NotBlank(message = "メールアドレスは必須です")
        @field:Email(message = "有効なメールアドレスを入力してください")
        val email: String,
        @field:NotBlank(message = "パスワードは必須です") val password: String
)
