package ru.nemodev.platform.core.integration.http.customizer

import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.RestClientProperties

class RestClientUrlCustomizer: RestClientPropertyCustomizer {

    override fun customize(builder: RestClient.Builder, properties: RestClientProperties) {
        builder.baseUrl(properties.url)
    }
}