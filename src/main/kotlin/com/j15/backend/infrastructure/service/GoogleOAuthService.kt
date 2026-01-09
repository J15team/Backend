package com.j15.backend.infrastructure.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.j15.backend.infrastructure.config.GoogleOAuthProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException

/** Google OAuth2.0サービス（インフラ層） GoogleのOAuth2.0 APIとの通信を担当 */
@Service
@EnableConfigurationProperties(GoogleOAuthProperties::class)
class GoogleOAuthService(private val properties: GoogleOAuthProperties) {
    private val webClient = WebClient.builder().build()

    companion object {
        private const val GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token"
        private const val GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo"
        private const val GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo"
    }

    /** Google OAuth2.0で取得したユーザー情報 */
    data class GoogleUserInfo(
            val id: String, // GoogleのユーザーID
            val email: String,
            val name: String,
            val picture: String? // プロフィール画像URL
    )

    /**
     * ID Tokenを検証してユーザー情報を取得（推奨方式） Google Identity Services (GIS) からのID Tokenを検証
     *
     * @param idToken フロントエンドから受け取ったID Token
     * @return Googleユーザー情報
     */
    fun verifyIdToken(idToken: String): GoogleUserInfo {
        try {
            val response =
                    webClient
                            .get()
                            .uri("$GOOGLE_TOKEN_INFO_URL?id_token=$idToken")
                            .retrieve()
                            .bodyToMono(IdTokenInfo::class.java)
                            .block()
                            ?: throw GoogleOAuthException("ID Tokenの検証に失敗しました")

            // Client IDの検証（セキュリティ上重要）
            if (response.aud != properties.clientId) {
                throw GoogleOAuthException("ID TokenのClient IDが一致しません")
            }

            // メールが検証済みかチェック
            if (response.emailVerified != "true") {
                throw GoogleOAuthException("メールアドレスが検証されていません")
            }

            return GoogleUserInfo(
                    id = response.sub,
                    email = response.email,
                    name = response.name ?: response.email.substringBefore("@"),
                    picture = response.picture
            )
        } catch (e: WebClientResponseException) {
            throw GoogleOAuthException("ID Tokenの検証に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /**
     * 認証コードをアクセストークンに交換（レガシー方式）
     *
     * @param authorizationCode フロントエンドから受け取った認証コード
     * @return アクセストークン
     */
    fun exchangeCodeForToken(authorizationCode: String): String {
        try {
            val response =
                    webClient
                            .post()
                            .uri(GOOGLE_TOKEN_URL)
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .body(
                                    BodyInserters.fromFormData("code", authorizationCode)
                                            .with("client_id", properties.clientId)
                                            .with("client_secret", properties.clientSecret)
                                            .with("redirect_uri", properties.redirectUri)
                                            .with("grant_type", "authorization_code")
                            )
                            .retrieve()
                            .bodyToMono(TokenResponse::class.java)
                            .block()
                            ?: throw GoogleOAuthException("Googleからのレスポンスが空です")

            return response.accessToken
        } catch (e: WebClientResponseException) {
            throw GoogleOAuthException("Googleトークン交換に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /**
     * アクセストークンを使用してGoogleユーザー情報を取得
     *
     * @param accessToken Googleアクセストークン
     * @return Googleユーザー情報
     */
    fun getUserInfo(accessToken: String): GoogleUserInfo {
        try {
            val response =
                    webClient
                            .get()
                            .uri(GOOGLE_USERINFO_URL)
                            .header("Authorization", "Bearer $accessToken")
                            .retrieve()
                            .bodyToMono(UserInfoResponse::class.java)
                            .block()
                            ?: throw GoogleOAuthException("ユーザー情報の取得に失敗しました")

            return GoogleUserInfo(
                    id = response.id,
                    email = response.email,
                    name = response.name,
                    picture = response.picture
            )
        } catch (e: WebClientResponseException) {
            throw GoogleOAuthException("Googleユーザー情報の取得に失敗しました: ${e.responseBodyAsString}")
        }
    }

    /** 認証コードから直接ユーザー情報を取得（レガシー方式） */
    fun authenticateWithCode(authorizationCode: String): GoogleUserInfo {
        val accessToken = exchangeCodeForToken(authorizationCode)
        return getUserInfo(accessToken)
    }

    // ID Token検証レスポンス
    private data class IdTokenInfo(
            val iss: String, // 発行者（https://accounts.google.com）
            val sub: String, // ユーザーID（GoogleのユニークID）
            val aud: String, // Client ID
            val email: String,
            @JsonProperty("email_verified") val emailVerified: String,
            val name: String? = null,
            val picture: String? = null,
            @JsonProperty("given_name") val givenName: String? = null,
            @JsonProperty("family_name") val familyName: String? = null
    )

    // Googleからのレスポンスをマッピングするデータクラス
    private data class TokenResponse(
            @JsonProperty("access_token") val accessToken: String,
            @JsonProperty("token_type") val tokenType: String,
            @JsonProperty("expires_in") val expiresIn: Int,
            val scope: String,
            @JsonProperty("id_token") val idToken: String? = null
    )

    private data class UserInfoResponse(
            val id: String,
            val email: String,
            val name: String,
            val picture: String? = null,
            @JsonProperty("verified_email") val verifiedEmail: Boolean = false
    )
}

/** Google OAuth認証時の例外 */
class GoogleOAuthException(message: String) : RuntimeException(message)
