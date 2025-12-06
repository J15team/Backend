package com.j15.backend.domain.model.subject

// 題材ID値オブジェクト
data class SubjectId(val value: Long) {
    init {
        require(value > 0) { "題材IDは正の値である必要があります" }
    }
}
