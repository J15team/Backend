package com.j15.backend.presentation.controller

import com.j15.backend.application.service.AuthService
import com.j15.backend.presentation.dto.auth.SignInRequest
import com.j15.backend.presentation.dto.auth.SignInResponse
import com.j15.backend.presentation.dto.auth.SignUpRequest
import com.j15.backend.presentation.dto.auth.SignUpResponse
import com.j15.backend.presentation.dto.auth.TokenRefreshRequest
import com.j15.backend.presentation.dto.auth.TokenRefreshResponse
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
            val response = authService.signIn(request)
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
            val response = authService.signUp(request)
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
            val accessToken = authService.refreshToken(request.refreshToken)
            ResponseEntity.ok(TokenRefreshResponse(accessToken = accessToken))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }
}
