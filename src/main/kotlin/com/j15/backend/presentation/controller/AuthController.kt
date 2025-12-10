package com.j15.backend.presentation.controller

import com.j15.backend.application.service.AuthService
import com.j15.backend.application.command.SignInCommand
import com.j15.backend.application.command.SignUpCommand
import com.j15.backend.application.command.RefreshTokenCommand
import com.j15.backend.presentation.dto.request.SignInRequest
import com.j15.backend.presentation.dto.response.SignInResponse
import com.j15.backend.presentation.dto.request.SignUpRequest
import com.j15.backend.presentation.dto.response.SignUpResponse
import com.j15.backend.presentation.dto.request.TokenRefreshRequest
import com.j15.backend.presentation.dto.response.TokenRefreshResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    /**
     * サインイン
     * POST /api/auth/signin
     */
    @PostMapping("/signin")
    fun signIn(@RequestBody request: SignInRequest): ResponseEntity<Any> {
        return try {
            val command = SignInCommand(
                email = request.email,
                password = request.password
            )
            val result = authService.signIn(command)
            val response = SignInResponse(
                userId = result.userId,
                email = result.email,
                accessToken = result.accessToken,
                refreshToken = result.refreshToken
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * サインアップ
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    fun signUp(@RequestBody request: SignUpRequest): ResponseEntity<Any> {
        return try {
            val command = SignUpCommand(
                email = request.email,
                username = request.username,
                password = request.password
            )
            val result = authService.signUp(command)
            val response = SignUpResponse(
                userId = result.userId,
                email = result.email,
                username = result.username
            )
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * トークンリフレッシュ
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@RequestBody request: TokenRefreshRequest): ResponseEntity<Any> {
        return try {
            val command = RefreshTokenCommand(
                refreshToken = request.refreshToken
            )
            val result = authService.refreshToken(command)
            val response = TokenRefreshResponse(
                accessToken = result.accessToken
            )
            ResponseEntity.ok(response)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }
}
