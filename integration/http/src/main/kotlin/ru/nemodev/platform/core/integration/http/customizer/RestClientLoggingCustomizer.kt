package ru.nemodev.platform.core.integration.http.customizer

import org.springframework.web.client.RestClient
import org.zalando.logbook.spring.LogbookClientHttpRequestInterceptor
import ru.nemodev.platform.core.integration.http.config.RestClientProperties

class RestClientLoggingCustomizer(
    private val logbookClientHttpRequestInterceptor: LogbookClientHttpRequestInterceptor
): RestClientPropertyCustomizer {

    override fun customize(builder: RestClient.Builder, properties: RestClientProperties) {
        if (properties.loggingEnabled) {
            builder.requestInterceptor(logbookClientHttpRequestInterceptor)
        }
    }
}
