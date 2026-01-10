package com.j15.backend.domain.model.tag

/** タグID値オブジェクト 0は新規作成時の仮IDとして許可 */
data class TagId(val value: Long) {
    init {
        require(value >= 0) { "タグIDは0以上である必要があります" }
    }

    fun isNew(): Boolean = value == 0L
}
