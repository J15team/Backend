package com.j15.backend.domain.model.user

import java.util.UUID

// ユーザーID（値オブジェクト）
data class UserId(val value: UUID = UUID.randomUUID()) {
    constructor(value: String) : this(UUID.fromString(value))

    override fun toString(): String = value.toString()
}
