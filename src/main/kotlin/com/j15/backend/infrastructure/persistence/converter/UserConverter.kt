package com.j15.backend.infrastructure.persistence.converter

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.OAuthProvider
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.UserRole
import com.j15.backend.domain.model.user.Username
import com.j15.backend.infrastructure.persistence.entity.UserEntity

/** UserエンティティとUserドメインモデル間の変換を担当するコンバーター */
object UserConverter {
        /** エンティティからドメインモデルへの変換 */
        fun toDomain(entity: UserEntity): User {
                return User(
                        userId =
                                UserId(
                                        entity.userId
                                                ?: throw IllegalStateException(
                                                        "userId must not be null"
                                                )
                                ),
                        username = Username(entity.username),
                        email = Email(entity.email),
                        passwordHash = entity.passwordHash?.let { PasswordHash(it) },
                        role = UserRole.fromString(entity.role),
                        profileImageUrl = entity.profileImageUrl,
                        oauthProvider = entity.oauthProvider?.let { OAuthProvider.fromValue(it) },
                        oauthProviderId = entity.oauthProviderId,
                        createdAt = entity.createdAt
                                        ?: throw IllegalStateException("createdAt must not be null")
                )
        }

        /** ドメインモデルからエンティティへの変換 */
        fun toEntity(domain: User): UserEntity {
                return UserEntity(
                        userId = domain.userId.value,
                        username = domain.username.value,
                        email = domain.email.value,
                        passwordHash = domain.passwordHash?.value,
                        role = domain.role.name,
                        profileImageUrl = domain.profileImageUrl,
                        oauthProvider = domain.oauthProvider?.value,
                        oauthProviderId = domain.oauthProviderId,
                        createdAt = domain.createdAt
                )
        }
}
