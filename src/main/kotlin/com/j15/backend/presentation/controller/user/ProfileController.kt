package com.j15.backend.presentation.controller.user

import com.j15.backend.application.usecase.user.ProfileUseCase
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.presentation.dto.profile.ProfileResponse
import com.j15.backend.presentation.dto.profile.ProfileUpdateResponse
import com.j15.backend.presentation.dto.profile.UpdateUsernameRequest
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

/** プロフィール管理コントローラー 責務: ユーザープロフィールの取得・更新 */
@RestController
@RequestMapping("/api/profile")
class ProfileController(private val profileUseCase: ProfileUseCase) {

    /** 自分のプロフィールを取得 */
    @GetMapping
    fun getMyProfile(@AuthenticationPrincipal userId: String): ResponseEntity<ProfileResponse> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            val user = profileUseCase.getProfile(userIdObj)
            ResponseEntity.ok(ProfileResponse.from(user))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /** プロフィール画像をアップロード・更新 */
    @PostMapping("/image")
    fun uploadProfileImage(
            @RequestParam("image") image: MultipartFile,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<ProfileUpdateResponse> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            val updatedUser = profileUseCase.updateProfileImage(userIdObj, image)
            ResponseEntity.ok(
                    ProfileUpdateResponse(
                            message = "プロフィール画像を更新しました",
                            profile = ProfileResponse.from(updatedUser)
                    )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                    .body(
                            ProfileUpdateResponse(
                                    message = e.message ?: "プロフィール画像の更新に失敗しました",
                                    profile =
                                            ProfileResponse(
                                                    userId = userId,
                                                    username = "",
                                                    email = "",
                                                    profileImageUrl = null,
                                                    createdAt = java.time.Instant.now()
                                            )
                            )
                    )
        }
    }

    /** プロフィール画像を削除 */
    @DeleteMapping("/image")
    fun deleteProfileImage(
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<ProfileUpdateResponse> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            val updatedUser = profileUseCase.deleteProfileImage(userIdObj)
            ResponseEntity.ok(
                    ProfileUpdateResponse(
                            message = "プロフィール画像を削除しました",
                            profile = ProfileResponse.from(updatedUser)
                    )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }

    /** ユーザー名を更新 */
    @PutMapping("/username")
    fun updateUsername(
            @RequestBody request: UpdateUsernameRequest,
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<ProfileUpdateResponse> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            val updatedUser = profileUseCase.updateUsername(userIdObj, request.username)
            ResponseEntity.ok(
                    ProfileUpdateResponse(
                            message = "ユーザー名を更新しました",
                            profile = ProfileResponse.from(updatedUser)
                    )
            )
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                    .body(
                            ProfileUpdateResponse(
                                    message = e.message ?: "ユーザー名の更新に失敗しました",
                                    profile =
                                            ProfileResponse(
                                                    userId = userId,
                                                    username = "",
                                                    email = "",
                                                    profileImageUrl = null,
                                                    createdAt = java.time.Instant.now()
                                            )
                            )
                    )
        }
    }

    /** アカウントを削除（関連する進捗データも削除される） */
    @DeleteMapping
    fun deleteAccount(
            @AuthenticationPrincipal userId: String
    ): ResponseEntity<Map<String, String>> {
        return try {
            val userIdObj = UserId(UUID.fromString(userId))
            profileUseCase.deleteAccount(userIdObj)
            ResponseEntity.ok(mapOf("message" to "アカウントを削除しました"))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
