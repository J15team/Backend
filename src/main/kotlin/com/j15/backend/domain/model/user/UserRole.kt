package com.j15.backend.domain.model.user

// ユーザーロール（値オブジェクト）
enum class UserRole {
    ROLE_USER,    // 一般ユーザー（自分の進捗のみ操作可能）
    ROLE_ADMIN;   // 管理者（題材の作成・更新・削除が可能）
    
    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name == value }
                ?: throw IllegalArgumentException("無効なロール: $value")
        }
    }
}
