package com.j15.backend.application.usecase.user

import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.UserDuplicationCheckService
import com.j15.backend.infrastructure.service.S3UploadService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

/** プロフィール管理ユースケース（アプリケーション層） ユーザープロフィールの取得・更新を担当 */
@Service
@Transactional
class ProfileUseCase(
        private val userRepository: UserRepository,
        private val duplicationCheckService: UserDuplicationCheckService,
        private val s3UploadService: S3UploadService
) {
    companion object {
        private const val PROFILE_IMAGES_FOLDER = "profile-images"
    }

    /** プロフィール情報を取得 */
    @Transactional(readOnly = true)
    fun getProfile(userId: UserId): User {
        return userRepository.findById(userId)
                ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")
    }

    /** プロフィール画像をアップロード・更新 */
    fun updateProfileImage(userId: UserId, file: MultipartFile): User {
        val user =
                userRepository.findById(userId)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")

        // 古い画像があれば削除
        user.profileImageUrl?.let { oldUrl ->
            try {
                s3UploadService.deleteImage(oldUrl)
            } catch (e: Exception) {
                // 削除失敗は無視（画像が既に存在しない場合など）
            }
        }

        // 新しい画像をS3にアップロード
        val imageUrl = s3UploadService.uploadImage(file, folder = PROFILE_IMAGES_FOLDER)

        // ユーザー情報を更新
        val updatedUser = user.copy(profileImageUrl = imageUrl)
        return userRepository.save(updatedUser)
    }

    /** プロフィール画像を削除 */
    fun deleteProfileImage(userId: UserId): User {
        val user =
                userRepository.findById(userId)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")

        // 画像がなければ何もしない
        val imageUrl = user.profileImageUrl ?: return user

        // S3から削除
        try {
            s3UploadService.deleteImage(imageUrl)
        } catch (e: Exception) {
            // 削除失敗は無視
        }

        // ユーザー情報を更新
        val updatedUser = user.copy(profileImageUrl = null)
        return userRepository.save(updatedUser)
    }

    /** ユーザー名を更新 */
    fun updateUsername(userId: UserId, newUsername: String): User {
        val user =
                userRepository.findById(userId)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")

        val usernameVo = Username(newUsername)

        // 同じユーザー名でなければ重複チェック
        if (user.username != usernameVo) {
            duplicationCheckService.checkUsernameAvailable(usernameVo)
        }

        val updatedUser = user.copy(username = usernameVo)
        return userRepository.save(updatedUser)
    }

    /** プロフィール全体を更新（画像＋ユーザー名） */
    fun updateProfile(userId: UserId, newUsername: String?, profileImage: MultipartFile?): User {
        var user =
                userRepository.findById(userId)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")

        // ユーザー名更新
        newUsername?.let { username ->
            val usernameVo = Username(username)
            if (user.username != usernameVo) {
                duplicationCheckService.checkUsernameAvailable(usernameVo)
                user = user.copy(username = usernameVo)
            }
        }

        // プロフィール画像更新
        profileImage?.let { file ->
            // 古い画像を削除
            user.profileImageUrl?.let { oldUrl ->
                try {
                    s3UploadService.deleteImage(oldUrl)
                } catch (e: Exception) {
                    // 削除失敗は無視
                }
            }
            // 新しい画像をアップロード
            val imageUrl = s3UploadService.uploadImage(file, folder = PROFILE_IMAGES_FOLDER)
            user = user.copy(profileImageUrl = imageUrl)
        }

        return userRepository.save(user)
    }

    /** アカウントを削除（関連する進捗データもCASCADE削除される） */
    fun deleteAccount(userId: UserId) {
        val user =
                userRepository.findById(userId)
                        ?: throw IllegalArgumentException("ユーザーが見つかりません: ${userId.value}")

        // プロフィール画像があれば削除
        user.profileImageUrl?.let { imageUrl ->
            try {
                s3UploadService.deleteImage(imageUrl)
            } catch (e: Exception) {
                // 削除失敗は無視（画像が既に存在しない場合など）
            }
        }

        userRepository.deleteById(userId)
    }
}
