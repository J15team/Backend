package com.j15.backend.presentation.controller.auth

import com.j15.backend.application.usecase.AuthUseCase
import com.j15.backend.presentation.dto.request.LoginRequest
import com.j15.backend.presentation.dto.response.LoginResponse
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

/** ユーザー認証コントローラー 責務: ユーザーのサインイン（認証） */
@RestController
@RequestMapping("/api/auth")
class SignInController(private val authUseCase: AuthUseCase) {

    @PostMapping("/signin")
    fun signIn(@Valid @RequestBody request: LoginRequest): LoginResponse {
        val user = authUseCase.authenticate(request.email, request.password)
        return LoginResponse(
                userId = user.userId.value.toString(),
                username = user.username.value,
                email = user.email.value
        )
    }
}
