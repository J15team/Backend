package com.j15.backend.presentation.controller

import com.j15.backend.application.usecase.RegisterUserCommand
import com.j15.backend.application.usecase.UserCommandUseCase
import com.j15.backend.presentation.dto.request.RegisterUserRequest
import com.j15.backend.presentation.dto.response.UserResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// ユーザー登録API
@RestController
@RequestMapping("/api/users")
class UserController(private val userCommandUseCase: UserCommandUseCase) {
    // ユーザー登録
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@Valid @RequestBody request: RegisterUserRequest): UserResponse {
        val command =
                RegisterUserCommand(
                        username = request.username,
                        email = request.email,
                        plainPassword = request.password
                )
        val user = userCommandUseCase.register(command)
        return UserResponse(
                userId = user.userId.value.toString(),
                username = user.username.value,
                email = user.email.value,
                createdAt = user.createdAt
        )
    }
}
