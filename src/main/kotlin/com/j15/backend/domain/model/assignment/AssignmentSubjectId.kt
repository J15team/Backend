package com.j15.backend.domain.model.assignment

/** 課題題材ID値オブジェクト */
@JvmInline
value class AssignmentSubjectId(val value: Long) {
    init {
        require(value > 0) { "課題題材IDは正の値である必要があります" }
    }
}
