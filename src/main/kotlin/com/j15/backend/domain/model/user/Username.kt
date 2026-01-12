package com.j15.backend.domain.model.user

// ユーザー名（値オブジェクト）表示用の名前
data class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "ユーザー名は空にできません" }
        require(value.length <= 50) { "ユーザー名は50文字以下である必要があります" }
    }
}
