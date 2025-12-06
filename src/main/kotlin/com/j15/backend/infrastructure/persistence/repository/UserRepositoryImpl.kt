package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.infrastructure.persistence.entity.UserEntity
import org.springframework.stereotype.Repository

// ユーザーリポジトリ実装（インフラ層）
@Repository
class UserRepositoryImpl(private val jpaUserRepository: JpaUserRepository) : UserRepository {

    override fun findById(id: UserId): User? {
        return jpaUserRepository.findById(id.value).map { it.toDomain() }.orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return jpaUserRepository.findByEmail(email.value)?.toDomain()
    }

    override fun existsByEmail(email: Email): Boolean = jpaUserRepository.existsByEmail(email.value)

    override fun existsByUsername(username: Username): Boolean =
            jpaUserRepository.existsByUsername(username.value)

    override fun save(user: User): User {
        val entity = user.toEntity()
        return jpaUserRepository.save(entity).toDomain()
    }

    override fun deleteById(id: UserId) {
        jpaUserRepository.deleteById(id.value)
    }

    // ドメインモデルとエンティティの変換
    private fun UserEntity.toDomain(): User {
        return User(
                userId = UserId(this.userId!!),
                username = Username(this.username),
                email = Email(this.email),
                passwordHash = PasswordHash(this.passwordHash),
                createdAt = this.createdAt!!
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
                userId = this.userId.value,
                username = this.username.value,
                email = this.email.value,
                passwordHash = this.passwordHash.value,
                createdAt = this.createdAt
        )
    }
}
