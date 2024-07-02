package ru.nemodev.platform.core.integration.http.factory

import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.HttpClientProperties
import ru.nemodev.platform.core.integration.http.customizer.RestClientPropertyCustomizer


interface RestClientFactory {
    fun create(properties: HttpClientProperties): RestClient
    fun createBuilder(properties: HttpClientProperties): RestClient.Builder
}

class RestClientFactoryImpl(
    private val restClientPropertyCustomizers: List<RestClientPropertyCustomizer>,
) : RestClientFactory {

    override fun create(properties: HttpClientProperties) = createBuilder(properties).build()

    override fun createBuilder(properties: HttpClientProperties): RestClient.Builder {
        return RestClient.builder().also { builder ->
            restClientPropertyCustomizers.forEach { it.customize(builder, properties) }
        }
    }
}
