package com.j15.backend.presentation.controller

import com.j15.backend.application.command.RefreshTokenCommand
import com.j15.backend.application.command.SignInCommand
import com.j15.backend.application.command.SignUpCommand
import com.j15.backend.application.service.AuthService
import com.j15.backend.domain.exception.DuplicateEmailException
import com.j15.backend.domain.exception.DuplicateUsernameException
import com.j15.backend.domain.exception.InvalidCredentialsException
import com.j15.backend.domain.exception.InvalidTokenException
import com.j15.backend.domain.exception.UserNotFoundException
import com.j15.backend.presentation.dto.auth.SignInRequest
import com.j15.backend.presentation.dto.auth.SignInResponse
import com.j15.backend.presentation.dto.auth.SignUpRequest
import com.j15.backend.presentation.dto.auth.SignUpResponse
import com.j15.backend.presentation.dto.auth.TokenRefreshRequest
import com.j15.backend.presentation.dto.auth.TokenRefreshResponse
import jakarta.validation.Valid
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
    fun signIn(@Valid @RequestBody request: SignInRequest): ResponseEntity<Any> {
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
        } catch (e: InvalidCredentialsException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * サインアップ
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<Any> {
        return try {
            val command = SignUpCommand(
                email = request.email,
                password = request.password,
                username = request.username
            )
            val result = authService.signUp(command)
            val response = SignUpResponse(
                userId = result.userId,
                email = result.email,
                username = result.username
            )
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: DuplicateEmailException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        } catch (e: DuplicateUsernameException) {
            ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("error" to e.message))
        }
    }

    /**
     * トークンリフレッシュ
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    fun refreshToken(@Valid @RequestBody request: TokenRefreshRequest): ResponseEntity<Any> {
        return try {
            val command = RefreshTokenCommand(refreshToken = request.refreshToken)
            val accessToken = authService.refreshToken(command)
            ResponseEntity.ok(TokenRefreshResponse(accessToken = accessToken))
        } catch (e: InvalidTokenException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        } catch (e: UserNotFoundException) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to e.message))
        }
    }
}
