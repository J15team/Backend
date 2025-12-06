package com.j15.backend.infrastructure.config

import com.j15.backend.domain.service.ProgressCalculationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainServiceConfig {

    @Bean
    fun progressCalculationService(): ProgressCalculationService {
        return ProgressCalculationService()
    }
}
