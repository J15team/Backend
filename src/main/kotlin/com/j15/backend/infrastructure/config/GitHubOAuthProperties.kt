package com.j15.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties

/** GitHub OAuth2.0の設定プロパティ application.ymlのgithub.oauthセクションから値を読み込む */
@ConfigurationProperties(prefix = "github.oauth")
data class GitHubOAuthProperties(val clientId: String, val clientSecret: String)
