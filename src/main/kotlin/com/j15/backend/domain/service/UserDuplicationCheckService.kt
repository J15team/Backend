package com.j15.backend.domain.service

import com.j15.backend.domain.exception.DuplicateEmailException
import com.j15.backend.domain.exception.DuplicateUsernameException
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository

// ユーザー重複チェックを担当するドメインサービス
class UserDuplicationCheckService(private val userRepository: UserRepository) {
    // メールアドレスが使用可能かチェック
    fun checkEmailAvailable(email: Email) {
        if (userRepository.existsByEmail(email)) {
            throw DuplicateEmailException()
        }
    }

    // ユーザー名が使用可能かチェック
    fun checkUsernameAvailable(username: Username) {
        if (userRepository.existsByUsername(username)) {
            throw DuplicateUsernameException()
        }
    }
}
