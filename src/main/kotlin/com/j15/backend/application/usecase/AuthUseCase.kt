package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.PasswordHashService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// 認証専用のアプリケーションサービス
@Service
@Transactional(readOnly = true)
class AuthUseCase(
    private val userRepository: UserRepository,
    private val passwordHashService: PasswordHashService
) {
    // タイミング攻撃を防ぐための有効なダミーBCryptハッシュ
    // "dummy_password"のBCryptハッシュ値
    private val dummyHash = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMye.DOH7R4rE/pqKQf9p1mT4cXQF7lXjvC"
    
    fun authenticate(email: String, plainPassword: String): User {
        val emailVo = Email(email)
        val user = userRepository.findByEmail(emailVo)
        
        // タイミング攻撃を防ぐため、ユーザーが存在しない場合でもパスワード検証を実行
        val isPasswordValid = if (user != null) {
            passwordHashService.verify(plainPassword, user.passwordHash.value)
        } else {
            // ダミーのハッシュで検証を実行してタイミングを一定に保つ
            passwordHashService.verify(plainPassword, dummyHash)
            false
        }
        
        if (user == null || !isPasswordValid) {
            throw IllegalArgumentException("メールアドレスまたはパスワードが正しくありません")
        }
        return user
    }
}
