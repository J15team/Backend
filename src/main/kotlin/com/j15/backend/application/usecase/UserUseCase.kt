package com.j15.backend.application.usecase

import com.j15.backend.domain.model.User
import com.j15.backend.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/** ユーザーユースケース（Application層） ビジネスロジックの調整とトランザクション管理を担当 */
@Service
@Transactional(readOnly = true)
class UserUseCase(private val userRepository: UserRepository) {
    /** ユーザーをIDで取得 */
    fun findById(id: Long): User? {
        return userRepository.findById(id)
    }

    /** メールアドレスでユーザーを取得 */
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    /** 全ユーザーを取得 */
    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    /** ユーザーを登録 */
    @Transactional
    fun register(username: String, email: String, passwordHash: String): User {
        // メールアドレスの重複チェック
        userRepository.findByEmail(email)?.let {
            throw IllegalArgumentException("このメールアドレスは既に登録されています")
        }
        // ユーザー名の重複チェックも追加
        userRepository.findByUsername(username)?.let {
            throw IllegalArgumentException("このユーザー名は既に使用されています")
        }

        val user = User(username = username, email = email, passwordHash = passwordHash)
        return userRepository.save(user)
    }

    /** ユーザーを削除 */
    @Transactional
    fun delete(id: Long) {
        userRepository.deleteById(id)
    }
}
