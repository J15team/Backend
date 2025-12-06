package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.application.query.UserQueryPort
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.infrastructure.persistence.entity.UserEntity
import org.springframework.stereotype.Repository

// 読み取り専用のクエリアダプタ（必要なら別DB/キャッシュに差し替え可能）
@Repository
class UserQueryAdapter(private val jpaUserRepository: JpaUserRepository) : UserQueryPort {
    override fun findAll(): List<User> = jpaUserRepository.findAll().map { it.toDomain() }
}

// 既存のマッピング関数を共有
private fun UserEntity.toDomain(): User {
    return User(
            userId = UserId(this.userId!!),
            username = Username(this.username),
            email = Email(this.email),
            passwordHash = PasswordHash(this.passwordHash),
            createdAt = this.createdAt!!
    )
}
