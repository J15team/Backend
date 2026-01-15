package com.j15.backend.domain.model.assignment

/** 提出ID値オブジェクト */
@JvmInline
value class SubmissionId(val value: Long) {
    init {
        require(value > 0) { "提出IDは正の値である必要があります" }
    }
}
