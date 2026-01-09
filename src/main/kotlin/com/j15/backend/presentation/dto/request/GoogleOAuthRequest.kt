package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.NotBlank

/** Google OAuth認証リクエストDTO フロントエンドから認証コードを受け取る */
data class GoogleOAuthRequest(@field:NotBlank(message = "認証コードは必須です") val code: String)
