package com.j15.backend.domain.repository

import com.j15.backend.domain.model.User

/** ユーザーリポジトリインターフェース（ドメイン層） 永続化の実装詳細から独立したインターフェース */
interface UserRepository {
    fun findById(id: Long): User?
    fun findByEmail(email: String): User?
    fun findAll(): List<User>
    fun save(user: User): User
    fun delete(user: User)
    fun deleteById(id: Long)
}
