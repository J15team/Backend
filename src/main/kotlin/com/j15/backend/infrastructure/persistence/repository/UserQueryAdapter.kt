package com.j15.backend.infrastructure.persistence.repository

import com.j15.backend.application.query.UserQueryPort
import com.j15.backend.domain.model.user.User
import com.j15.backend.infrastructure.persistence.converter.UserConverter
import org.springframework.stereotype.Repository

// 読み取り専用のクエリアダプタ（必要なら別DB/キャッシュに差し替え可能）
@Repository
class UserQueryAdapter(private val jpaUserRepository: JpaUserRepository) : UserQueryPort {
    override fun findAll(): List<User> =
            jpaUserRepository.findAll().map { UserConverter.toDomain(it) }
}
