package com.j15.backend.domain.model.user

// ユーザー名（値オブジェクト）表示用の名前
data class Username(val value: String) {
    init {
        require(value.length in 3..20) { "ユーザー名は3文字以上20文字以下である必要があります" }
        require(value.matches(Regex("^[a-zA-Z0-9_]+$"))) { "ユーザー名は英数字とアンダースコアのみ使用可能です" }
    }
}
