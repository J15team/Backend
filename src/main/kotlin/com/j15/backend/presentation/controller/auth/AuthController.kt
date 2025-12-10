package com.j15.backend.presentation.controller.auth

import com.j15.backend.application.usecase.auth.AuthUseCase
import com.j15.backend.application.usecase.auth.TokenManagementUseCase
import com.j15.backend.application.usecase.user.RegisterUserCommand
import com.j15.backend.application.usecase.user.UserCommandUseCase
import com.j15.backend.presentation.dto.request.LoginRequest
import com.j15.backend.presentation.dto.request.RefreshTokenRequest
import com.j15.backend.presentation.dto.request.RegisterUserRequest
import com.j15.backend.presentation.dto.response.LoginResponse
import com.j15.backend.presentation.dto.response.RefreshTokenResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
        private val authUseCase: AuthUseCase,
        private val tokenManagementUseCase: TokenManagementUseCase,
        private val userCommandUseCase: UserCommandUseCase
) {

        @PostMapping("/signin")
        fun signIn(@Valid @RequestBody request: LoginRequest): LoginResponse {
                val result = authUseCase.authenticate(request.email, request.password)

                return LoginResponse(
                        accessToken = result.tokens.accessToken.value,
                        refreshToken = result.tokens.refreshToken.value,
                        user =
                                LoginResponse.UserInfo(
                                        id = result.user.userId.value.toString(),
                                        username = result.user.username.value,
                                        email = result.user.email.value
                                )
                )
        }

        @PostMapping("/signup")
        @ResponseStatus(HttpStatus.CREATED)
        fun signUp(@Valid @RequestBody request: RegisterUserRequest): LoginResponse {
                val result =
                        userCommandUseCase.registerAndGenerateTokens(
                                RegisterUserCommand(
                                        username = request.username,
                                        email = request.email,
                                        plainPassword = request.password
                                )
                        )

                return LoginResponse(
                        accessToken = result.tokens.accessToken.value,
                        refreshToken = result.tokens.refreshToken.value,
                        user =
                                LoginResponse.UserInfo(
                                        id = result.user.userId.value.toString(),
                                        username = result.user.username.value,
                                        email = result.user.email.value
                                )
                )
        }

        /** Refresh Tokenを使用してAccess Tokenを更新 */
        @PostMapping("/refresh")
        fun refresh(@Valid @RequestBody request: RefreshTokenRequest): RefreshTokenResponse {
                val tokens = tokenManagementUseCase.refreshAccessToken(request.refreshToken)

                return RefreshTokenResponse(
                        accessToken = tokens.accessToken.value,
                        refreshToken = tokens.refreshToken.value
                )
        }
}
