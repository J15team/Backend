package com.j15.backend.infrastructure.security

import com.j15.backend.domain.service.JwtTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT認証フィルター
 *
 * Bearer Tokenを検証しSecurityContextに認証情報をセット。 401エラーは返さず、SecurityConfigが処理する。
 */
@Component
class JwtAuthenticationFilter(private val jwtTokenService: JwtTokenService) :
        OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        try {
            val token = extractTokenFromRequest(request)

            if (token != null && jwtTokenService.validateToken(token)) {
                val userId = jwtTokenService.getUserIdFromToken(token)
                val role = jwtTokenService.getRoleFromToken(token)
                val authorities = listOf(SimpleGrantedAuthority(role.name))

                val authentication =
                        UsernamePasswordAuthenticationToken(
                                userId.value.toString(),
                                null,
                                authorities
                        )
                authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authentication
            }
        } catch (e: Exception) {
            // 例外発生時もSecurityContextは空のまま
            // 401エラーはSecurityConfigが返す
            logger.error("JWT認証エラーが発生しました")
            if (logger.isDebugEnabled) {
                logger.debug("JWT認証エラー詳細: ${e.message}", e)
            }
        }

        // Step 4: 後続処理へ委譲（認証成功・失敗に関わらず必ず実行）
        filterChain.doFilter(request, response)
    }

    /**
     * AuthorizationヘッダーからBearer Tokenを抽出
     *
     * 期待するヘッダー形式: Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     *
     * @param request HTTPリクエスト
     * @return JWT Token文字列（"Bearer "を除いたもの）、存在しない場合はnull
     */
    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7) // "Bearer "（7文字）を除去
        } else {
            null
        }
    }
}
