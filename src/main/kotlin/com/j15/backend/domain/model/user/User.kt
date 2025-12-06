package com.j15.backend.domain.model.user

import java.time.Instant

// ユーザーエンティティ（ドメイン層）永続化の詳細から独立したドメインモデル
data class User(
        val userId: UserId,
        val username: Username,
        val email: Email,
        val createdAt: Instant = Instant.now()
// val passwordHash: PasswordHash // 後ほど実装
)
