package ru.nemodev.platform.core.openapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springdoc.core.configuration.*
import org.springdoc.core.models.GroupedOpenApi
import org.springdoc.core.properties.SpringDocConfigProperties
import org.springdoc.core.properties.SwaggerUiConfigParameters
import org.springdoc.core.properties.SwaggerUiConfigProperties
import org.springdoc.core.properties.SwaggerUiOAuthProperties
import org.springdoc.webmvc.core.configuration.MultipleOpenApiSupportConfiguration
import org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration
import org.springdoc.webmvc.ui.SwaggerConfig
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.ConfigurableEnvironment
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.platform.core.openapi.customizer.OpenApiExampleLoader
import ru.nemodev.platform.core.openapi.processor.OpenApiBeanDefinitionRegistryPostProcessor
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration(before = [
    SpringDocWebMvcConfiguration::class,
    MultipleOpenApiSupportConfiguration::class,
    SwaggerConfig::class,
    SwaggerUiConfigProperties::class,
    SwaggerUiConfigParameters::class,
    SwaggerUiOAuthProperties::class,
    SpringDocUIConfiguration::class,
    SpringDocConfiguration::class,
    SpringDocConfigProperties::class,
    SpringDocJavadocConfiguration::class,
    SpringDocGroovyConfiguration::class,
    SpringDocSecurityConfiguration::class,
    SpringDocFunctionCatalogConfiguration::class,
    SpringDocHateoasConfiguration::class,
    SpringDocPageableConfiguration::class,
    SpringDocSortConfiguration::class,
    SpringDocSpecPropertiesConfiguration::class,
    SpringDocDataRestConfiguration::class,
    SpringDocKotlinConfiguration::class,
    SpringDocKotlinxConfiguration::class
])
@PropertySource(value = ["classpath:core-open-api.yml"], factory = YamlPropertySourceFactory::class)
@EnableConfigurationProperties(OpenApiProperties::class)
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
class OpenApiConfiguration {

    @Bean
    @ConditionalOnBean(type = ["org.springdoc.core.models.GroupedOpenApi"])
    fun openApiExampleLoader(objectMapper: ObjectMapper) = OpenApiExampleLoader(objectMapper)

    @Bean
    fun apiClientConfiguration(environment: ConfigurableEnvironment) =
        OpenApiBeanDefinitionRegistryPostProcessor(environment)

    // TODO: придумать решение получше, чем создание и удаление бина
    /*
    Магическое место. Этот бин требуется создать, чтобы выполнилось условие MultipleOpenApiSupportCondition,
    от которого зависит включение MultipleOpenApiSupportConfiguration.
    Бины этого же типа создаются в BeanDefinitionRegistryPostProcessor,
    но шаг пост-процессора выполняется после работы Conditional.
    После создания бин выпиливается в OpenApiBeanDefinitionRegistryPostProcessor.
     */
    @Bean("removeMe")
    fun disposableGroupedOpenApi(): GroupedOpenApi = GroupedOpenApi.builder()
        .group("removeMe")
        .packagesToScan("removeMe")
        .build()
}
