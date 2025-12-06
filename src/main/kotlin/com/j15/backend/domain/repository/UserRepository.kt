package com.j15.backend.domain.repository

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username

// ユーザーリポジトリインターフェース(ドメイン層)
interface UserRepository {
    fun findById(id: UserId): User?
    fun findByEmail(email: Email): User?
    fun existsByEmail(email: Email): Boolean
    fun existsByUsername(username: Username): Boolean
    fun save(user: User): User
    fun deleteById(id: UserId)
}
