package com.j15.backend.domain.model.auth

// リフレッシュトークンの値オブジェクト
data class RefreshToken(val value: String) {
    init {
        require(value.isNotBlank()) { "リフレッシュトークンは空にできません" }
    }
}
