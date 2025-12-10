package com.j15.backend.application.usecase.user

import com.j15.backend.application.query.UserQueryPort
import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// ユーザー参照専用のユースケース（集約取得とリードモデルを分離）
@Service
@Transactional(readOnly = true)
class UserQueryUseCase(
        private val userRepository: UserRepository,
        private val userQueryPort: UserQueryPort
) {
    fun findById(id: String): User? = userRepository.findById(UserId(id))
    fun findByEmail(email: String): User? = userRepository.findByEmail(Email(email))
    fun findAll(): List<User> = userQueryPort.findAll()
}
