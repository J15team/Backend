package com.j15.backend.domain.repository

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username

// ユーザーリポジトリインターフェース（ドメイン層）
interface UserRepository {
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun findByUsername(username: Username): User?
    fun findAll(): List<User>
    fun save(user: User): User
    fun delete(user: User)
    fun deleteById(id: UserId)
}
