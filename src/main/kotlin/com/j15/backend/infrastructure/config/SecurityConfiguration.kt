package com.j15.backend.infrastructure.config

import com.j15.backend.infrastructure.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
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
class SecurityConfiguration(private val jwtAuthenticationFilter: JwtAuthenticationFilter) {
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
                            .requestMatchers("/api/auth/signin", "/api/users/signup", "/api/health")
                            .permitAll()
                            // Actuatorエンドポイント（監視用）
                            .requestMatchers("/actuator/**")
                            .permitAll()
                            // 題材・進捗APIは認証必須
                            .requestMatchers("/api/subjects/**", "/api/progress/**")
                            .authenticated()
                            // その他のエンドポイントも認証必須
                            .anyRequest()
                            .authenticated()
                }
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter::class.java
                )
        return http.build()
    }
}
