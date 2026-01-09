package com.j15.backend.infrastructure.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.j15.backend.infrastructure.config.GitHubOAuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/** GitHub OAuth2.0サービス（インフラ層） GitHubのOAuth2.0 APIとの通信を担当 */
@Service
@EnableConfigurationProperties(GitHubOAuthProperties::class)
class GitHubOAuthService(private val properties: GitHubOAuthProperties) {
    private val webClient = WebClient.builder().build()

    companion object {
        private const val GITHUB_TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val GITHUB_USER_URL = "https://api.github.com/user"
        private const val GITHUB_EMAILS_URL = "https://api.github.com/user/emails"
    }

    /** GitHub OAuth2.0で取得したユーザー情報 */
    data class GitHubUserInfo(
            val id: String,
            val email: String,
            val name: String,
            val avatarUrl: String?
    )

    /**
     * 認証コードをアクセストークンに交換
     *
     * @param code フロントエンドから受け取った認証コード
     * @return アクセストークン
     */
    fun exchangeCodeForToken(code: String): String {
        try {
            val response =
                    webClient
                            .post()
                            .uri(GITHUB_TOKEN_URL)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .accept(MediaType.APPLICATION_JSON)
                            .body(
                                    BodyInserters.fromFormData("code", code)
                                            .with("client_id", properties.clientId)
                                            .with("client_secret", properties.clientSecret)
                            )
                            .retrieve()
                            .bodyToMono(TokenResponse::class.java)
                            .block()
                            ?: throw GitHubOAuthException("GitHubからのレスポンスが空です")

            if (response.error != null) {
                throw GitHubOAuthException(
                        "GitHubトークン交換エラー: ${response.errorDescription ?: response.error}"
                )
            }

            return response.accessToken ?: throw GitHubOAuthException("アクセストークンが取得できませんでした")
        } catch (e: WebClientResponseException) {
            throw GitHubOAuthException("GitHubトークン交換に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /**
     * アクセストークンを使用してGitHubユーザー情報を取得
     *
     * @param accessToken GitHubアクセストークン
     * @return GitHubユーザー情報
     */
    fun getUserInfo(accessToken: String): GitHubUserInfo {
        try {
            val userResponse =
                    webClient
                            .get()
                            .uri(GITHUB_USER_URL)
                            .header("Authorization", "Bearer $accessToken")
                            .header("Accept", "application/vnd.github+json")
                            .retrieve()
                            .bodyToMono(UserResponse::class.java)
                            .block()
                            ?: throw GitHubOAuthException("ユーザー情報の取得に失敗しました")

            // メールがnullの場合、emails APIから取得
            val email = userResponse.email ?: getPrimaryEmail(accessToken)

            return GitHubUserInfo(
                    id = userResponse.id.toString(),
                    email = email,
                    name = userResponse.name ?: userResponse.login,
                    avatarUrl = userResponse.avatarUrl
            )
        } catch (e: WebClientResponseException) {
            throw GitHubOAuthException("GitHubユーザー情報の取得に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /** プライマリメールアドレスを取得 */
    private fun getPrimaryEmail(accessToken: String): String {
        try {
            val emails =
                    webClient
                            .get()
                            .uri(GITHUB_EMAILS_URL)
                            .header("Authorization", "Bearer $accessToken")
                            .header("Accept", "application/vnd.github+json")
                            .retrieve()
                            .bodyToFlux(EmailResponse::class.java)
                            .collectList()
                            .block()
                            ?: throw GitHubOAuthException("メールアドレスの取得に失敗しました")

            return emails.find { it.primary && it.verified }?.email
                    ?: emails.find { it.verified }?.email
                            ?: throw GitHubOAuthException("検証済みのメールアドレスが見つかりません")
        } catch (e: WebClientResponseException) {
            throw GitHubOAuthException("メールアドレスの取得に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /** 認証コードから直接ユーザー情報を取得 */
    fun authenticateWithCode(code: String): GitHubUserInfo {
        val accessToken = exchangeCodeForToken(code)
        return getUserInfo(accessToken)
    }

    private data class TokenResponse(
            @JsonProperty("access_token") val accessToken: String? = null,
            @JsonProperty("token_type") val tokenType: String? = null,
            val scope: String? = null,
            val error: String? = null,
            @JsonProperty("error_description") val errorDescription: String? = null
    )

    private data class UserResponse(
            val id: Long,
            val login: String,
            val name: String? = null,
            val email: String? = null,
            @JsonProperty("avatar_url") val avatarUrl: String? = null
    )

    private data class EmailResponse(
            val email: String,
            val primary: Boolean,
            val verified: Boolean
    )
}

/** GitHub OAuth認証時の例外 */
class GitHubOAuthException(message: String) : RuntimeException(message)
