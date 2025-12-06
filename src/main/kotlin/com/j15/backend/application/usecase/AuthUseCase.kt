package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.repository.UserRepository
import java.security.MessageDigest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 認証専用のアプリケーションサービス
@Service
@Transactional(readOnly = true)
class AuthUseCase(private val userRepository: UserRepository) {
    fun authenticate(email: String, plainPassword: String): User {
        val emailVo = Email(email)
        val user =
                userRepository.findByEmail(emailVo)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません")
        val hashed = hashPassword(plainPassword)
        if (user.passwordHash.value != hashed) throw IllegalArgumentException("パスワードが正しくありません")
        return user
    }

    private fun hashPassword(raw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
