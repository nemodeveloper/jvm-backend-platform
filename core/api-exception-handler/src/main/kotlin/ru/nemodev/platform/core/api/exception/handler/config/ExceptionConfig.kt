package ru.nemodev.platform.core.api.exception.handler.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import ru.nemodev.platform.core.api.exception.handler.ApiExceptionHandler

@AutoConfiguration
@EnableConfigurationProperties(ApiExceptionHandlerProperties::class)
class ExceptionConfig {

    @Bean
    fun apiExceptionHandler(
        property: ApiExceptionHandlerProperties,
        observationRegistry: ObservationRegistry
    ) = ApiExceptionHandler(property, observationRegistry)
}