package com.j15.backend.domain.service

import com.j15.backend.domain.repository.UserRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// ドメインサービスのBean定義
@Configuration
class UserDomainServiceConfig {
    @Bean
    fun userDuplicationCheckService(userRepository: UserRepository): UserDuplicationCheckService {
        return UserDuplicationCheckService(userRepository)
    }
}
