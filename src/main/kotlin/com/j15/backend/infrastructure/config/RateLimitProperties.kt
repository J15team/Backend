package com.j15.backend.infrastructure.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * レート制限の設定プロパティ
 * 
 * application.ymlのrate-limit設定を読み込む
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
data class RateLimitProperties(
    /** レート制限が有効かどうか */
    var enabled: Boolean = true,
    /** バケットの容量（最大リクエスト数） */
    var capacity: Long = 100,
    /** 初期トークン数 */
    var tokens: Long = 100,
    /** 補充するトークン数 */
    var refillTokens: Long = 100,
    /** トークン補充間隔（秒） */
    var refillPeriodSeconds: Long = 60
)
