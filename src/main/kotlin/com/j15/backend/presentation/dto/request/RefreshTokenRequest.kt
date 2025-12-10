package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.NotBlank

data class RefreshTokenRequest(
        @field:NotBlank(message = "リフレッシュトークンは必須です") val refreshToken: String
)
