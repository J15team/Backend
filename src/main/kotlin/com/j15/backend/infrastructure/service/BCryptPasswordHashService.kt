package com.j15.backend.infrastructure.service

import com.j15.backend.domain.service.PasswordHashService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

// BCryptを使用したパスワードハッシュ化サービスの実装
@Service
class BCryptPasswordHashService(
    private val passwordEncoder: PasswordEncoder
) : PasswordHashService {
    override fun hash(plainPassword: String): String {
        return passwordEncoder.encode(plainPassword)
    }

    override fun verify(plainPassword: String, hashedPassword: String): Boolean {
        return try {
            passwordEncoder.matches(plainPassword, hashedPassword)
        } catch (e: Exception) {
            // 不正なハッシュ形式の場合もタイミング攻撃を防ぐため例外を隠蔽
            false
        }
    }
}
