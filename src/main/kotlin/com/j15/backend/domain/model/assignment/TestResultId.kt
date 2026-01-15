package com.j15.backend.domain.model.assignment

/** テスト結果ID値オブジェクト */
@JvmInline
value class TestResultId(val value: Long) {
    init {
        require(value > 0) { "テスト結果IDは正の値である必要があります" }
    }
}
