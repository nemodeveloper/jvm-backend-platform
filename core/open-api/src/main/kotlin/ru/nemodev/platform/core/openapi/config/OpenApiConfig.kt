package ru.nemodev.platform.core.openapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.platform.core.openapi.customizer.OpenApiExampleCustomizer
import ru.nemodev.platform.core.openapi.customizer.OpenApiServersCustomizer
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@PropertySource(value = ["classpath:core-open-api.yml"], factory = YamlPropertySourceFactory::class)
@SecurityScheme(
    name = "apiKeyAuth",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.HEADER,
    paramName = ApiHeaderNames.API_KEY
)
@SecurityScheme(
    name = "bearerAuth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig {

    @Bean
    fun openApiExampleLoader(
        objectMapper: ObjectMapper
    ) = OpenApiExampleCustomizer(objectMapper)

    @Bean
    fun openApiServersCustomizer(
        properties: SpringDocConfigProperties
    ) = OpenApiServersCustomizer(properties)
}
