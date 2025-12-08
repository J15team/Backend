package com.j15.backend.domain.model.auth

// アクセストークンの値オブジェクト
data class AccessToken(val value: String) {
    init {
        require(value.isNotBlank()) { "アクセストークンは空にできません" }
    }
}
