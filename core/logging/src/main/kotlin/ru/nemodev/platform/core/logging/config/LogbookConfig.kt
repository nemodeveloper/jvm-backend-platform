package ru.nemodev.platform.core.logging.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.zalando.logbook.*
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration
import org.zalando.logbook.autoconfigure.LogbookProperties
import org.zalando.logbook.core.*
import org.zalando.logbook.core.Conditions.exclude
import org.zalando.logbook.core.Conditions.requestTo
import org.zalando.logbook.json.AccessTokenBodyFilter
import org.zalando.logbook.json.JacksonJsonFieldBodyFilter
import ru.nemodev.platform.core.logging.constant.Masking
import ru.nemodev.platform.core.logging.constant.PathFilterPatterns
import ru.nemodev.platform.core.logging.logbook.*
import ru.nemodev.platform.core.service.generator.IdGeneratorService
import java.util.function.Predicate

@AutoConfiguration(before = [LogbookAutoConfiguration::class])
@EnableConfigurationProperties(LoggingProperties::class)
class LogbookConfig {

    @Bean
    @ConditionalOnProperty(name = ["logbook.format.style"], havingValue = "platform", matchIfMissing = true)
    fun logbookHttpLogFormatter(
        objectMapper: ObjectMapper,
        loggingProperties: LoggingProperties
    ): HttpLogFormatter = LogbookHttpLogFormatter(objectMapper, loggingProperties)

    @Bean
    fun logbookHttpLogWriter(): HttpLogWriter = LogbookHttpLogWriter()

    @Bean
    fun logbookStrategy(loggingProperties: LoggingProperties): Strategy =
        LogbookStrategy(loggingProperties)

    @Bean
    fun logbookCorrelationId(idGeneratorService: IdGeneratorService): CorrelationId =
        LogbookCorrelationId(idGeneratorService)

    @Bean
    fun requestCondition(): Predicate<HttpRequest> = exclude(PathFilterPatterns.default.map { requestTo(it) })

    @Bean
    fun logbookHeaderFilter(logbookProperties: LogbookProperties): HeaderFilter {
        val headers = mutableSetOf<String>()
        headers.addAll(logbookProperties.obfuscate.headers)
        headers.addAll(Masking.maskingHeaders)

        return HeaderFilter.merge(
            HeaderFilters.defaultValue(),
            HeaderFilters.replaceHeaders(
                headers.toSet(),
                logbookProperties.obfuscate.replacement
            ),
        )
    }

    @Bean
    fun logbookQueryFilter(logbookProperties: LogbookProperties): QueryFilter {
        val headers = logbookProperties.obfuscate.headers.toSet()
        return QueryFilter.merge(
            QueryFilters.defaultValue(),
            QueryFilters.replaceQuery(
                { headers.contains(it) },
                logbookProperties.obfuscate.replacement
            )
        )
    }

    @Bean
    fun logbookDefaultRequestFilter(): RequestFilter = RequestFilters.defaultValue()

    @Bean
    fun logbookDefaultResponseFilter(): ResponseFilter = ResponseFilters.defaultValue()

    @Bean
    fun logbookDefaultBodyFilter(): BodyFilter = BodyFilters.defaultValue()

    @Bean
    fun logbookJsonPathBodyFilter(
        loggingProperties: LoggingProperties,
        logbookProperties: LogbookProperties,
    ) = LogbookJsonPathBodyFilter(loggingProperties, logbookProperties)

    @Bean
    fun logbookJacksonJsonFieldBodyFilter(logbookProperties: LogbookProperties, objectMapper: ObjectMapper): JacksonJsonFieldBodyFilter =
        object : JacksonJsonFieldBodyFilter(
            logbookProperties.obfuscate.jsonBodyFields,
            logbookProperties.obfuscate.replacement,
            objectMapper.factory
        ) {
            override fun filter(contentType: String?, body: String): String {
                // Базовая реализация всегда парсит тело даже если jsonBodyFields пустое
                if (logbookProperties.obfuscate.jsonBodyFields.isEmpty()) {
                    return body
                }
                return super.filter(contentType, body)
            }
        }

    @Bean
    fun logbookAccessTokenBodyFilter(): BodyFilter = AccessTokenBodyFilter()

    @Bean
    fun logbookOauthRequestBodyFilter(): BodyFilter = BodyFilters.oauthRequest()
}
