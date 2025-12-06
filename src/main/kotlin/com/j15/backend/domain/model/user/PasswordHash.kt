package com.j15.backend.domain.model.user

// パスワードハッシュ（値オブジェクト）
data class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "パスワードハッシュは空にできません" }
        require(value.length <= 255) { "パスワードハッシュが長すぎます" }
    }
}
