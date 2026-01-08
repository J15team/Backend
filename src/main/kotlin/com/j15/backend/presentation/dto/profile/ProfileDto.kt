package com.j15.backend.presentation.dto.profile

import com.j15.backend.domain.model.user.User
import java.time.Instant

/** プロフィールレスポンスDTO */
data class ProfileResponse(
        val userId: String,
        val username: String,
        val email: String,
        val profileImageUrl: String?,
        val createdAt: Instant
) {
    companion object {
        fun from(user: User): ProfileResponse {
            return ProfileResponse(
                    userId = user.userId.value.toString(),
                    username = user.username.value,
                    email = user.email.value,
                    profileImageUrl = user.profileImageUrl,
                    createdAt = user.createdAt
            )
        }
    }
}

/** ユーザー名更新リクエストDTO */
data class UpdateUsernameRequest(val username: String)

/** プロフィール更新成功レスポンス */
data class ProfileUpdateResponse(val message: String, val profile: ProfileResponse)
