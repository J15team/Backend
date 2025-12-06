package com.j15.backend.domain.model.user

// メールアドレス（値オブジェクト）ログインIDとして使用
data class Email(val value: String) {
    init {
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) { "無効なメールアドレス形式です" }
    }
}
