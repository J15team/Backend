package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.infrastructure.persistence.entity.UserEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserRepository : JpaRepository<UserEntity, UUID> {
    fun findByEmail(email: String): UserEntity?
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
}
