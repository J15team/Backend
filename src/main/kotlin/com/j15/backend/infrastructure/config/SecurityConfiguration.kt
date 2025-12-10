package com.j15.backend.infrastructure.config

import com.j15.backend.infrastructure.security.JwtAuthenticationFilter
import com.j15.backend.infrastructure.security.RateLimitFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

// セキュリティ関連のBean定義
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfiguration(
        private val jwtAuthenticationFilter: JwtAuthenticationFilter,
        private val rateLimitFilter: RateLimitFilter
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
                .csrf { it.disable() }
                .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
                .authorizeHttpRequests { auth ->
                    auth
                            // 認証不要の公開エンドポイント
                            .requestMatchers("/api/auth/signin", "/api/auth/signup", "/api/auth/refresh", "/api/health")
                            .permitAll()
                            // Actuatorエンドポイント（監視用）
                            .requestMatchers("/actuator/**")
                            .permitAll()
                            // 題材の読み取り（GET）は公開
                            .requestMatchers(HttpMethod.GET, "/api/subjects/**")
                            .permitAll()
                            // 題材の作成・更新・削除は認証必須
                            .requestMatchers(HttpMethod.POST, "/api/subjects/**")
                            .authenticated()
                            .requestMatchers(HttpMethod.PUT, "/api/subjects/**")
                            .authenticated()
                            .requestMatchers(HttpMethod.DELETE, "/api/subjects/**")
                            .authenticated()
                            // 進捗APIは認証必須
                            .requestMatchers("/api/progress/**")
                            .authenticated()
                            // その他のエンドポイントも認証必須
                            .anyRequest()
                            .authenticated()
                }
                // フィルター適用順序（重要）:
                // 1. RateLimitFilter - 認証処理前にレート制限を適用（ブルートフォース攻撃対策）
                // 2. JwtAuthenticationFilter - JWT認証処理
                // 3. UsernamePasswordAuthenticationFilter - Spring Securityデフォルト
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
                .addFilterBefore(jwtAuthenticationFilter, rateLimitFilter::class.java)
        return http.build()
    }
}
