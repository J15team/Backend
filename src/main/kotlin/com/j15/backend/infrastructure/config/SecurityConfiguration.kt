package com.j15.backend.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

// セキュリティ関連のBean定義
@Configuration
@EnableWebSecurity
class SecurityConfiguration {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { it.disable() }.authorizeHttpRequests { auth ->
            auth.requestMatchers(
                            "/api/auth/**",
                            "/api/users/signup",
                            "/api/health",
                            "/api/subjects/**",
                            "/api/sections/**",
                            "/api/progress/**"
                    )
                    .permitAll()
                    .requestMatchers("/actuator/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
        }
        return http.build()
    }
}
