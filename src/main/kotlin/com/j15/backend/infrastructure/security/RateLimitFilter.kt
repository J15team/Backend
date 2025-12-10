package com.j15.backend.infrastructure.security

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.j15.backend.infrastructure.config.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * レート制限フィルター
 *
 * IPアドレスごとにリクエスト数を制限し、DDOS攻撃から保護する
 *
 * セキュリティ対策:
 * - LRUキャッシュによるメモリリーク防止
 * - 信頼できるプロキシの検証によるX-Forwarded-For偽装対策
 * - 認証処理前に適用してブルートフォース攻撃を防止
 */
@Component
class RateLimitFilter(private val rateLimitProperties: RateLimitProperties) :
        OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RateLimitFilter::class.java)

    /** IPアドレスごとのバケットを保持するLRUキャッシュ */
    private val bucketCache: Cache<String, Bucket> =
            Caffeine.newBuilder().maximumSize(rateLimitProperties.maxCacheSize).build()

    /** 信頼できるプロキシのIPアドレスセット */
    private val trustedProxies: Set<String> =
            rateLimitProperties
                    .trustedProxies
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        // レート制限が無効の場合はスキップ
        if (!rateLimitProperties.enabled) {
            filterChain.doFilter(request, response)
            return
        }

        val clientIp = getClientIp(request)
        val bucket = resolveBucket(clientIp)

        // トークンを消費できるか確認
        if (bucket.tryConsume(1)) {
            // レート制限情報をヘッダーに追加
            val availableTokens = bucket.availableTokens
            response.setHeader("X-RateLimit-Limit", rateLimitProperties.capacity.toString())
            response.setHeader("X-RateLimit-Remaining", availableTokens.toString())

            filterChain.doFilter(request, response)
        } else {
            // レート制限超過
            logger.warn("Rate limit exceeded for IP: $clientIp on path: ${request.requestURI}")
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.setHeader("X-RateLimit-Limit", rateLimitProperties.capacity.toString())
            response.setHeader("X-RateLimit-Remaining", "0")
            response.writer.write(
                    """
                {
                    "error": "Too Many Requests",
                    "message": "リクエスト数が制限を超えました。しばらく待ってから再度お試しください。"
                }
            """.trimIndent()
            )
        }
    }

    /**
     * IPアドレスに対応するバケットを取得または作成
     *
     * Caffeineのget()メソッドは、キャッシュにエントリが存在しない場合に 指定されたロード関数を実行してバケットを作成します。
     * LRUポリシーにより、古いエントリは自動的に削除されます。
     */
    private fun resolveBucket(clientIp: String): Bucket {
        return bucketCache.get(clientIp) { createNewBucket() }!!
    }

    /** 新しいバケットを作成 */
    private fun createNewBucket(): Bucket {
        val limit =
                Bandwidth.builder()
                        .capacity(rateLimitProperties.capacity)
                        .refillIntervally(
                                rateLimitProperties.refillTokens,
                                Duration.ofSeconds(rateLimitProperties.refillPeriodSeconds)
                        )
                        .build()
        return Bucket.builder().addLimit(limit).build()
    }

    /**
     * クライアントのIPアドレスを取得
     *
     * セキュリティ対策:
     * - 直接接続元が信頼できるプロキシの場合のみX-Forwarded-Forヘッダーを使用
     * - それ以外の場合は直接接続元のIPアドレスを使用
     * - これにより、X-Forwarded-Forヘッダーの偽装によるレート制限回避を防止
     *
     * 設定方法: application.ymlのrate-limit.trusted-proxiesに信頼できるプロキシのIPアドレスを カンマ区切りで指定してください（例:
     * "10.0.0.1,10.0.0.2"）
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        // 直接接続元が信頼できるプロキシの場合のみX-Forwarded-Forを使用
        if (trustedProxies.isNotEmpty() && remoteAddr in trustedProxies) {
            val xForwardedFor = request.getHeader("X-Forwarded-For")
            if (xForwardedFor != null && xForwardedFor.isNotEmpty()) {
                val clientIp = xForwardedFor.split(",")[0].trim()
                logger.debug("Using X-Forwarded-For IP: $clientIp from trusted proxy: $remoteAddr")
                return clientIp
            }
        }

        return remoteAddr
    }
}
