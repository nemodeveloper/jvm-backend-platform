package ru.nemodev.platform.core.integration.http.customizer

import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.HttpClientProperties


interface RestClientPropertyCustomizer {
    fun customize(builder: RestClient.Builder, properties: HttpClientProperties)
}