package com.j15.backend.domain.model.user

import java.time.Instant

/**
 * ユーザーエンティティ（ドメイン層） 永続化の詳細から独立したドメインモデル
 *
 * OAuth認証の場合、passwordHashはnullになる
 */
data class User(
        val userId: UserId,
        val username: Username,
        val email: Email,
        val passwordHash: PasswordHash?,
        val role: UserRole = UserRole.ROLE_USER,
        val profileImageUrl: String? = null,
        val oauthProvider: OAuthProvider? = null,
        val oauthProviderId: String? = null,
        val loginCount: Int = 0,
        val lastLoginAt: Instant? = null,
        val createdAt: Instant = Instant.now()
) {
        /** 通常のメール/パスワード認証ユーザーかどうか */
        val isLocalUser: Boolean
                get() = oauthProvider == null

        /** OAuth認証ユーザーかどうか */
        val isOAuthUser: Boolean
                get() = oauthProvider != null

        /** 初回ログインかどうか（チュートリアル表示用） */
        val isFirstLogin: Boolean
                get() = loginCount == 0
}
