package com.j15.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** Google OAuth2.0の設定プロパティ application.ymlのgoogle.oauthセクションから値を読み込む */
@ConfigurationProperties(prefix = "google.oauth")
data class GoogleOAuthProperties(
        val clientId: String,
        val clientSecret: String,
        val redirectUri: String
)
