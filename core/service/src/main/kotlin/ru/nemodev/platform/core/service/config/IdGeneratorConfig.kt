package ru.nemodev.platform.core.service.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import ru.nemodev.platform.core.service.generator.IdGeneratorService
import ru.nemodev.platform.core.service.generator.IdGeneratorServiceImpl

@AutoConfiguration
class IdGeneratorConfig {

    @Bean
    fun idGeneratorService(): IdGeneratorService = IdGeneratorServiceImpl()
}