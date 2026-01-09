package com.j15.backend.presentation.dto.response

/** OAuth認証レスポンスDTO 通常のログインレスポンスに加え、新規登録かどうかのフラグを含む */
data class OAuthLoginResponse(
        val accessToken: String,
        val refreshToken: String,
        val user: UserInfo,
        val isNewUser: Boolean, // 新規登録されたユーザーかどうか
        val message: String
) {
    data class UserInfo(
            val id: String,
            val username: String,
            val email: String,
            val profileImageUrl: String? = null
    )
}
