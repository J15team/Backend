package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.infrastructure.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

/** Spring Data JPA リポジトリ（インフラ層） JpaRepositoryを継承したデータアクセス用インターフェース */
interface JpaUserRepository : JpaRepository<UserEntity, Long> {
    fun findByEmail(email: String): UserEntity?
}
