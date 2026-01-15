package com.j15.backend.domain.model.assignment

/** サポートするプログラミング言語 */
enum class Language {
    C; // 初期対応としてC言語のみ

    companion object {
        fun fromString(value: String): Language? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
