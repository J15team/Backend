package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.AuthUseCase
import com.j15.backend.presentation.dto.request.LoginRequest
import com.j15.backend.presentation.dto.response.LoginResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

// 認証API
@RestController
@RequestMapping("/api/auth")
class AuthController(private val authUseCase: AuthUseCase) {
    // ログイン
    @PostMapping("/signin")
    fun login(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val user = authUseCase.authenticate(request.email, request.password)
        return LoginResponse(
                userId = user.userId.value.toString(),
                username = user.username.value,
                email = user.email.value
        )
    }
}
