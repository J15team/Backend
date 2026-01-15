package com.j15.backend.infrastructure.client

import org.springframework.boot.context.properties.ConfigurationProperties

/** Judge Service設定 */
@ConfigurationProperties(prefix = "judge-service")
data class JudgeServiceProperties(
        /** Judge ServiceのベースURL */
        val baseUrl: String = "http://localhost:8081",

        /** タイムアウト（ミリ秒） */
        val timeout: Long = 30000,

        /** リトライ回数 */
        val retryCount: Int = 3
)
