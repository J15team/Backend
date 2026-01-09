package com.j15.backend.presentation.dto.request

import jakarta.validation.constraints.NotBlank

/** GitHub OAuth認証リクエストDTO */
data class GitHubOAuthRequest(@field:NotBlank(message = "認証コードは必須です") val code: String)
