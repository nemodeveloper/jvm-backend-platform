package ru.nemodev.platform.core.integration.http.factory

import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import ru.nemodev.platform.core.integration.http.customizer.RestClientPropertyCustomizer


interface RestClientFactory {
    fun create(properties: RestClientProperties): RestClient
    fun createBuilder(properties: RestClientProperties): RestClient.Builder
}

class RestClientFactoryImpl(
    private val restClientPropertyCustomizers: List<RestClientPropertyCustomizer>,
) : RestClientFactory {

    override fun create(properties: RestClientProperties) = createBuilder(properties).build()

    override fun createBuilder(properties: RestClientProperties): RestClient.Builder {
        return RestClient.builder().also { builder ->
            restClientPropertyCustomizers.forEach { it.customize(builder, properties) }
        }
    }
}
