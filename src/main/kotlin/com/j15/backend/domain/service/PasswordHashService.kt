package com.j15.backend.domain.service

// パスワードハッシュ化サービス（ドメインサービスインターフェース）
interface PasswordHashService {
    fun hash(plainPassword: String): String
    fun verify(plainPassword: String, hashedPassword: String): Boolean
}
