package com.j15.backend.domain.model

import java.time.Instant

/** ユーザーエンティティ（ドメイン層） 永続化の詳細から独立したドメインモデル */
data class User(
        val userId: Long? = null,
        val username: String,
        val email: String,
        val passwordHash: String,
        val createdAt: Instant? = null
)
