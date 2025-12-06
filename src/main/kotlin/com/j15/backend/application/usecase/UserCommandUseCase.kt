package com.j15.backend.application.usecase

import com.j15.backend.domain.model.user.Email
import com.j15.backend.domain.model.user.PasswordHash
import com.j15.backend.domain.model.user.User
import com.j15.backend.domain.model.user.UserId
import com.j15.backend.domain.model.user.Username
import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.PasswordHashService
import com.j15.backend.domain.service.UserDuplicationCheckService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

// ユーザー登録・削除などコマンド系ユースケース
@Service
class UserCommandUseCase(
        private val userRepository: UserRepository,
        private val duplicationCheckService: UserDuplicationCheckService,
        private val passwordHashService: PasswordHashService
) {
    @Transactional
    fun register(command: RegisterUserCommand): User {
        val emailVo = Email(command.email)
        val usernameVo = Username(command.username)
        val passwordHash = PasswordHash(passwordHashService.hash(command.plainPassword))

        duplicationCheckService.checkEmailAvailable(emailVo)
        duplicationCheckService.checkUsernameAvailable(usernameVo)

        val user =
                User(
                        userId = UserId(),
                        username = usernameVo,
                        email = emailVo,
                        passwordHash = passwordHash
                )
        return userRepository.save(user)
    }

    @Transactional
    fun delete(id: String) {
        userRepository.deleteById(UserId(id))
    }
}

// ユーザー登録コマンド
data class RegisterUserCommand(val username: String, val email: String, val plainPassword: String)
