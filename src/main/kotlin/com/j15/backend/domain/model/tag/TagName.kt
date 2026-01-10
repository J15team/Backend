package com.j15.backend.domain.model.tag

/** タグ名値オブジェクト 1-50文字、英数字・日本語・ハイフン・アンダースコアのみ許可 */
data class TagName(val value: String) {
    init {
        require(value.isNotBlank()) { "タグ名は空にできません" }
        require(value.length in MIN_LENGTH..MAX_LENGTH) {
            "タグ名は${MIN_LENGTH}〜${MAX_LENGTH}文字である必要があります"
        }
        require(isValidCharacters(value)) { "タグ名に使用できない文字が含まれています" }
    }

    /** #プレフィックス付きの表示用文字列を返す */
    fun toDisplayString(): String = "#$value"

    companion object {
        const val MIN_LENGTH = 1
        const val MAX_LENGTH = 50

        // 英数字、ひらがな、カタカナ、漢字、ハイフン、アンダースコアを許可
        private val VALID_PATTERN =
                Regex("^[a-zA-Z0-9\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FAF_-]+$")

        fun isValidCharacters(value: String): Boolean = VALID_PATTERN.matches(value)
    }
}
