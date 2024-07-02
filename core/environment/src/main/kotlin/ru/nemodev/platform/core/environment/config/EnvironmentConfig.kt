package ru.nemodev.platform.core.environment.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.environment.service.EnvironmentServiceImpl

@AutoConfiguration
class EnvironmentConfig {

    @Bean
    fun environmentService(): EnvironmentService = EnvironmentServiceImpl()
}