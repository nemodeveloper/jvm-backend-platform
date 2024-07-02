package ru.nemodev.platform.core.integration.http.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.zalando.logbook.autoconfigure.LogbookAutoConfiguration
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor
import ru.nemodev.platform.core.integration.http.customizer.*
import ru.nemodev.platform.core.integration.http.factory.RestClientFactory
import ru.nemodev.platform.core.integration.http.factory.RestClientFactoryImpl

@AutoConfiguration
@Import(LogbookAutoConfiguration::class, )
class RestClientConfiguration {

    @Bean
    fun restClientFactory(
        restClientPropertyCustomizers: List<RestClientPropertyCustomizer>,
    ) : RestClientFactory = RestClientFactoryImpl(restClientPropertyCustomizers)

    @Bean
    fun restClientUrlCustomizer() = RestClientUrlCustomizer()

    @Bean
    fun restClientDefaultHeadersCustomizer() = RestClientDefaultHeadersCustomizer()

    @Bean
    fun restClientHttpClientCustomizer() = RestClientHttpClientCustomizer()

    @Bean
    fun restClientObservationCustomizer(
        observationRegistry: ObservationRegistry
    ) = RestClientObservationCustomizer(observationRegistry)

    @Bean
    fun httpClientLoggingCustomizer(
        logbookClientHttpRequestInterceptor: LogbookClientHttpRequestInterceptor
    ) = RestClientLoggingCustomizer(logbookClientHttpRequestInterceptor)
}