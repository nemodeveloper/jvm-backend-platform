package ru.nemodev.platform.core.integration.http.customizer

import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.HttpClientProperties

class RestClientUrlCustomizer: RestClientPropertyCustomizer {

    override fun customize(builder: RestClient.Builder, properties: HttpClientProperties) {
        builder.baseUrl(properties.url)
    }
}