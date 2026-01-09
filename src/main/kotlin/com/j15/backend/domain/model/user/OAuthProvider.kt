package com.j15.backend.domain.model.user

/** OAuth認証プロバイダーを表す値オブジェクト */
enum class OAuthProvider(val value: String) {
    GOOGLE("google"),
    GITHUB("github"),
    ;

    companion object {
        fun fromValue(value: String): OAuthProvider {
            return entries.find { it.value == value }
                    ?: throw IllegalArgumentException("不正なOAuthプロバイダー: $value")
        }
    }
}
