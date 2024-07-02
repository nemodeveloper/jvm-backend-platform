package ru.nemodev.platform.core.integration.http.customizer

import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.HttpClientProperties

class RestClientDefaultHeadersCustomizer: RestClientPropertyCustomizer {

    override fun customize(builder: RestClient.Builder, properties: HttpClientProperties) {
        builder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON.toString())

        properties.headers?.forEach { header ->
            builder.defaultHeader(header.name, header.value)
        }
    }
}