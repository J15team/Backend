package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import java.security.MessageDigest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// ユーザーのアプリケーションサービス
@Service
@Transactional(readOnly = true)
class UserUseCase(private val userRepository: UserRepository) {
    // ユーザーをIDで取得
    fun findById(id: String): User? = userRepository.findById(UserId(id))

    // メールアドレスでユーザーを取得
    fun findByEmail(email: String): User? = userRepository.findByEmail(Email(email))

    // 全ユーザーを取得
    fun findAll(): List<User> = userRepository.findAll()

    // ユーザーを登録
    @Transactional
    fun register(command: RegisterUserCommand): User {
        val emailVo = Email(command.email)
        val usernameVo = Username(command.username)
        val passwordHash = PasswordHash(hashPassword(command.plainPassword))

        userRepository.findByEmail(emailVo)?.let {
            throw IllegalArgumentException("このメールアドレスは既に登録されています")
        }
        userRepository.findByUsername(usernameVo)?.let {
            throw IllegalArgumentException("このユーザー名は既に使用されています")
        }

        val user =
                User(
                        userId = UserId(),
                        username = usernameVo,
                        email = emailVo,
                        passwordHash = passwordHash
                )
        return userRepository.save(user)
    }

    // メール + パスワードで認証
    fun authenticate(email: String, plainPassword: String): User {
        val emailVo = Email(email)
        val user =
                userRepository.findByEmail(emailVo)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません")
        val hashed = hashPassword(plainPassword)
        if (user.passwordHash.value != hashed) throw IllegalArgumentException("パスワードが正しくありません")
        return user
    }

    // ユーザーを削除
    @Transactional
    fun delete(id: String) {
        userRepository.deleteById(UserId(id))
    }

    private fun hashPassword(raw: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}

// ユーザー登録コマンド
data class RegisterUserCommand(val username: String, val email: String, val plainPassword: String)
