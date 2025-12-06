package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.infrastructure.persistence.converter.UserConverter
import org.springframework.stereotype.Repository

// ユーザーリポジトリ実装（インフラ層）
@Repository
class UserRepositoryImpl(private val jpaUserRepository: JpaUserRepository) : UserRepository {

    override fun findById(id: UserId): User? {
        return jpaUserRepository.findById(id.value).map { UserConverter.toDomain(it) }.orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        return jpaUserRepository.findByEmail(email.value)?.let { UserConverter.toDomain(it) }
    }

    override fun existsByEmail(email: Email): Boolean = jpaUserRepository.existsByEmail(email.value)

    override fun existsByUsername(username: Username): Boolean =
            jpaUserRepository.existsByUsername(username.value)

    override fun save(user: User): User {
        val entity = UserConverter.toEntity(user)
        return UserConverter.toDomain(jpaUserRepository.save(entity))
    }

    override fun deleteById(id: UserId) {
        jpaUserRepository.deleteById(id.value)
    }
}
