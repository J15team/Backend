package com.j15.backend.infrastructure.security

import com.j15.backend.infrastructure.config.RateLimitProperties
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

/**
 * レート制限フィルター
 * 
 * IPアドレスごとにリクエスト数を制限し、DDOS攻撃から保護する
 */
@Component
class RateLimitFilter(
    private val rateLimitProperties: RateLimitProperties
) : OncePerRequestFilter() {

    /** IPアドレスごとのバケットを保持するマップ */
    private val bucketCache = ConcurrentHashMap<String, Bucket>()

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
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
            response.setHeader("X-RateLimit-Limit", rateLimitProperties.capacity.toString())
            response.setHeader("X-RateLimit-Remaining", "0")
            response.writer.write("""
                {
                    "error": "Too Many Requests",
                    "message": "リクエスト数が制限を超えました。しばらく待ってから再度お試しください。"
                }
            """.trimIndent())
        }
    }

    /**
     * IPアドレスに対応するバケットを取得または作成
     */
    private fun resolveBucket(clientIp: String): Bucket {
        return bucketCache.computeIfAbsent(clientIp) { 
            // キャッシュサイズが上限に達している場合の対応
            // computeIfAbsentはスレッドセーフだが、サイズ制限は緩い制約として実装
            if (bucketCache.size >= rateLimitProperties.maxCacheSize) {
                // サイズ上限に達している場合でも新規バケットを作成
                // （エントリの自動削除は行わないが、新規リクエストは制限される）
                createNewBucket()
            } else {
                createNewBucket()
            }
        }
    }

    /**
     * 新しいバケットを作成
     */
    private fun createNewBucket(): Bucket {
        val limit = Bandwidth.builder()
            .capacity(rateLimitProperties.capacity)
            .refillIntervally(
                rateLimitProperties.refillTokens,
                Duration.ofSeconds(rateLimitProperties.refillPeriodSeconds)
            )
            .build()
        return Bucket.builder()
            .addLimit(limit)
            .build()
    }

    /**
     * クライアントのIPアドレスを取得
     * 
     * プロキシ経由の場合はX-Forwarded-Forヘッダーから取得
     * 
     * 注意: X-Forwarded-Forヘッダーは改ざん可能なため、
     * 信頼できるプロキシのみを使用する環境で運用することを推奨します。
     * 本番環境では、信頼できるプロキシIPの検証を追加することを検討してください。
     */
    private fun getClientIp(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        return if (xForwardedFor != null && xForwardedFor.isNotEmpty()) {
            xForwardedFor.split(",")[0].trim()
        } else {
            request.remoteAddr
        }
    }
}
