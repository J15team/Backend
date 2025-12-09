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
            filterChain.doFilter(request, response)
        } else {
            // レート制限超過
            response.status = HttpStatus.TOO_MANY_REQUESTS.value()
            response.contentType = "application/json"
            response.characterEncoding = "UTF-8"
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
        return bucketCache.computeIfAbsent(clientIp) { createNewBucket() }
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
