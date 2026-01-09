package com.j15.backend.domain.model.user

/** OAuth認証プロバイダーを表す値オブジェクト */
enum class OAuthProvider(val value: String) {
    GOOGLE("google"),
    // 将来的に他のプロバイダーを追加可能
    // GITHUB("github"),
    // LINE("line"),
    ;

    companion object {
        fun fromValue(value: String): OAuthProvider {
            return entries.find { it.value == value }
                    ?: throw IllegalArgumentException("不正なOAuthプロバイダー: $value")
        }
    }
}
