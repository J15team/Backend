package com.j15.backend.presentation.controller.auth

import com.j15.backend.application.usecase.auth.AuthUseCase
import com.j15.backend.application.usecase.auth.GitHubOAuthUseCase
import com.j15.backend.application.usecase.auth.GoogleOAuthUseCase
import com.j15.backend.application.usecase.auth.TokenManagementUseCase
import com.j15.backend.application.usecase.user.RegisterUserCommand
import com.j15.backend.application.usecase.user.UserCommandUseCase
import com.j15.backend.presentation.dto.request.GitHubOAuthRequest
import com.j15.backend.presentation.dto.request.GoogleIdTokenRequest
import com.j15.backend.presentation.dto.request.GoogleOAuthRequest
import com.j15.backend.presentation.dto.request.LoginRequest
import com.j15.backend.presentation.dto.request.RefreshTokenRequest
import com.j15.backend.presentation.dto.request.RegisterUserRequest
import com.j15.backend.presentation.dto.response.LoginResponse
import com.j15.backend.presentation.dto.response.OAuthLoginResponse
import com.j15.backend.presentation.dto.response.RefreshTokenResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
        private val authUseCase: AuthUseCase,
        private val googleOAuthUseCase: GoogleOAuthUseCase,
        private val gitHubOAuthUseCase: GitHubOAuthUseCase,
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

        /**
         * Google OAuth2.0認証（ID Token方式 - 推奨） Google Identity Services (GIS) のポップアップログインで取得したID
         * Tokenを検証
         */
        @PostMapping("/google/token")
        fun googleIdToken(@Valid @RequestBody request: GoogleIdTokenRequest): OAuthLoginResponse {
                val result = googleOAuthUseCase.authenticateWithIdToken(request.credential)

                return OAuthLoginResponse(
                        accessToken = result.tokens.accessToken.value,
                        refreshToken = result.tokens.refreshToken.value,
                        user =
                                OAuthLoginResponse.UserInfo(
                                        id = result.user.userId.value.toString(),
                                        username = result.user.username.value,
                                        email = result.user.email.value,
                                        profileImageUrl = result.user.profileImageUrl
                                ),
                        isNewUser = result.isNewUser,
                        message = if (result.isNewUser) "アカウントを作成しました" else "ログインに成功しました"
                )
        }

        /** Google OAuth2.0認証（Authorization Code方式 - レガシー） フロントエンドから受け取った認証コードを使用してログイン/登録を行う */
        @PostMapping("/google")
        fun googleOAuth(@Valid @RequestBody request: GoogleOAuthRequest): OAuthLoginResponse {
                val result = googleOAuthUseCase.authenticateWithGoogle(request.code)

                return OAuthLoginResponse(
                        accessToken = result.tokens.accessToken.value,
                        refreshToken = result.tokens.refreshToken.value,
                        user =
                                OAuthLoginResponse.UserInfo(
                                        id = result.user.userId.value.toString(),
                                        username = result.user.username.value,
                                        email = result.user.email.value,
                                        profileImageUrl = result.user.profileImageUrl
                                ),
                        isNewUser = result.isNewUser,
                        message = if (result.isNewUser) "アカウントを作成しました" else "ログインに成功しました"
                )
        }

        /** GitHub OAuth2.0認証 フロントエンドから受け取った認証コードを使用してログイン/登録を行う */
        @PostMapping("/github")
        fun gitHubOAuth(@Valid @RequestBody request: GitHubOAuthRequest): OAuthLoginResponse {
                val result = gitHubOAuthUseCase.authenticateWithGitHub(request.code)

                return OAuthLoginResponse(
                        accessToken = result.tokens.accessToken.value,
                        refreshToken = result.tokens.refreshToken.value,
                        user =
                                OAuthLoginResponse.UserInfo(
                                        id = result.user.userId.value.toString(),
                                        username = result.user.username.value,
                                        email = result.user.email.value,
                                        profileImageUrl = result.user.profileImageUrl
                                ),
                        isNewUser = result.isNewUser,
                        message = if (result.isNewUser) "アカウントを作成しました" else "ログインに成功しました"
                )
        }
}
