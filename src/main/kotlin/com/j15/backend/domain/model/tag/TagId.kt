package com.j15.backend.domain.model.tag

/** タグID値オブジェクト */
data class TagId(val value: Long) {
    init {
        require(value > 0) { "タグIDは正の値である必要があります" }
    }
}
