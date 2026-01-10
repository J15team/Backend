package com.j15.backend.domain.model.user

// ユーザーロール（値オブジェクト）
enum class UserRole {
    ROLE_USER, // 一般ユーザー（自分の進捗のみ操作可能）
    ROLE_ADMIN, // 管理者（題材の作成・更新・削除が可能）
    ROLE_DEVELOPER; // 開発者（最上位権限、全操作可能）

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name == value }
                    ?: throw IllegalArgumentException("無効なロール: $value")
        }

        /**
         * 権限階層の比較 ROLE_DEVELOPER > ROLE_ADMIN > ROLE_USER
         * @return role1がrole2以上の権限を持つ場合true
         */
        fun isHigherOrEqual(role1: UserRole, role2: UserRole): Boolean {
            val hierarchy = mapOf(ROLE_USER to 0, ROLE_ADMIN to 1, ROLE_DEVELOPER to 2)
            return hierarchy[role1]!! >= hierarchy[role2]!!
        }
    }
}
