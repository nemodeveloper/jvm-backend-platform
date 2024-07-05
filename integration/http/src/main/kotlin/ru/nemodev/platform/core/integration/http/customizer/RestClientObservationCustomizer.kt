package ru.nemodev.platform.core.integration.http.customizer

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.actuate.metrics.web.client.ObservationRestClientCustomizer
import org.springframework.http.client.observation.ClientRequestObservationContext
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention
import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.RestClientProperties

class RestClientObservationCustomizer(
    observationRegistry: ObservationRegistry
): RestClientPropertyCustomizer {

    private val observationRestClientCustomizer = ObservationRestClientCustomizer(
        observationRegistry,
        DefaultClientRequestObservationConvention()
    )

    override fun customize(builder: RestClient.Builder, properties: RestClientProperties) {
        if (properties.observationEnabled) {
            observationRestClientCustomizer.customize(builder)
            builder.observationConvention(
                object : DefaultClientRequestObservationConvention() {
                    /**
                     * Костыль для проставления информации в контекст
                     * т.к нет возможности повлиять на создание контекста
                     */
                    override fun supportsContext(context: Observation.Context): Boolean {
                        val support = super.supportsContext(context)
                        if (support && context is ClientRequestObservationContext) {
                            context.apply {
                                remoteServiceName = properties.serviceId
                            }
                        }
                        return support
                    }
                }
            )
        }
    }
}