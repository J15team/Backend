package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.User
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.infrastructure.persistence.entity.UserEntity
import org.springframework.stereotype.Repository

/** ユーザーリポジトリ実装（インフラ層） ドメイン層のインターフェースを実装し、JPAを使用して永続化 */
@Repository
class UserRepositoryImpl(private val jpaUserRepository: JpaUserRepository) : UserRepository {

    override fun findById(id: Long): User? {
        return jpaUserRepository.findById(id).map { it.toDomain() }.orElse(null)
    }

    override fun findByEmail(email: String): User? {
        return jpaUserRepository.findByEmail(email)?.toDomain()
    }

    override fun findAll(): List<User> {
        return jpaUserRepository.findAll().map { it.toDomain() }
    }

    override fun save(user: User): User {
        val entity = user.toEntity()
        return jpaUserRepository.save(entity).toDomain()
    }

    override fun delete(user: User) {
        user.userId?.let { jpaUserRepository.deleteById(it) }
    }

    override fun deleteById(id: Long) {
        jpaUserRepository.deleteById(id)
    }

    // ドメインモデルとエンティティの変換
    private fun UserEntity.toDomain(): User {
        return User(
                userId = this.userId,
                username = this.username,
                email = this.email,
                passwordHash = this.passwordHash,
                createdAt = this.createdAt
        )
    }

    private fun User.toEntity(): UserEntity {
        return UserEntity(
                userId = this.userId,
                username = this.username,
                email = this.email,
                passwordHash = this.passwordHash,
                createdAt = this.createdAt
        )
    }
}
