package com.j15.backend.domain.model.assignment

/** 課題セクションID値オブジェクト */
@JvmInline
value class AssignmentSectionId(val value: Int) {
    init {
        require(value >= 0) { "課題セクションIDは0以上である必要があります" }
    }
}
