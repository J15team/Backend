package com.j15.backend.domain.service

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository

// ユーザー重複チェックを担当するドメインサービス
class UserDuplicationCheckService(private val userRepository: UserRepository) {
    // メールアドレスが使用可能かチェック
    fun checkEmailAvailable(email: Email) {
        if (userRepository.findByEmail(email) != null) {
            throw IllegalArgumentException("このメールアドレスは既に登録されています")
        }
    }

    // ユーザー名が使用可能かチェック
    fun checkUsernameAvailable(username: Username) {
        if (userRepository.findByUsername(username) != null) {
            throw IllegalArgumentException("このユーザー名は既に使用されています")
        }
    }
}
