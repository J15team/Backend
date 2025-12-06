package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// ユーザーユースケース（Application層）
@Service
@Transactional(readOnly = true)
class UserUseCase(private val userRepository: UserRepository) {
    // ユーザーをIDで取得
    fun findById(id: String): User? {
        return userRepository.findById(UserId(id))
    }

    // メールアドレスでユーザーを取得
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(Email(email))
    }

    // 全ユーザーを取得
    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    // ユーザーを登録
    @Transactional
    fun register(username: String, email: String): User {
        val emailVo = Email(email)
        val usernameVo = Username(username)

        // メールアドレスの重複チェック
        userRepository.findByEmail(emailVo)?.let {
            throw IllegalArgumentException("このメールアドレスは既に登録されています")
        }
        // ユーザー名の重複チェックも追加
        userRepository.findByUsername(usernameVo)?.let {
            throw IllegalArgumentException("このユーザー名は既に使用されています")
        }

        val user = User(userId = UserId(), username = usernameVo, email = emailVo)
        return userRepository.save(user)
    }

    // ユーザーを削除
    @Transactional
    fun delete(id: String) {
        userRepository.deleteById(UserId(id))
    }
}
