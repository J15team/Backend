package com.j15.backend.infrastructure.service

import com.j15.backend.domain.repository.UserRepository
import com.j15.backend.domain.service.UserDuplicationCheckService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// ドメインサービスのBean定義（インフラ層）
@Configuration
class DomainServiceConfiguration {
    @Bean
    fun userDuplicationCheckService(userRepository: UserRepository): UserDuplicationCheckService {
        return UserDuplicationCheckService(userRepository)
    }
}
