package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.NotBlank

/** Google ID Token認証リクエストDTO（推奨方式） Google Identity Services (GIS) から受け取ったID Tokenを送信 */
data class GoogleIdTokenRequest(
        @field:NotBlank(message = "ID Tokenは必須です")
        val credential: String // Google Sign-Inのcredential（ID Token）
)
