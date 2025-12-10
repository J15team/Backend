package com.j15.backend.infrastructure.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authz ->
                authz
                    // ヘルスチェック、Actuator は認証不要
                    .requestMatchers("/api/health", "/actuator/**").permitAll()
                    // 認証関連エンドポイント（サインイン、サインアップ）は認証不要
                    .requestMatchers(HttpMethod.POST, "/api/auth/signin", "/api/auth/signup").permitAll()
                    // 管理者エンドポイント（APIキーで保護、コントローラー内で検証）
                    // TODO: Spring Securityの認証フローに統合してセキュリティ監査を改善
                    .requestMatchers("/api/admin/**").permitAll()
                    // 題材の取得（GET）は認証不要
                    .requestMatchers(HttpMethod.GET, "/api/subjects/**").permitAll()
                    // 題材の作成・更新・削除は ROLE_ADMIN のみ
                    .requestMatchers(HttpMethod.POST, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/subjects/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/subjects/**").hasRole("ADMIN")
                    // その他のすべてのエンドポイントは認証必須
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
