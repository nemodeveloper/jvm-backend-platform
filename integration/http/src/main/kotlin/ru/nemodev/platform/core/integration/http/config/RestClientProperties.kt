package ru.nemodev.platform.core.integration.http.config

import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.core.io.Resource
import org.springframework.http.HttpMethod
import java.time.Duration

data class RestClientProperties(
    val serviceId: String,
    val url: String,
    @DefaultValue
    @NestedConfigurationProperty
    val timeout: Timeout,
    @DefaultValue("true")
    val loggingEnabled: Boolean,
    @DefaultValue("true")
    val observationEnabled: Boolean,
    @DefaultValue("false")
    val redirectEnabled: Boolean,
    @NestedConfigurationProperty
    val proxy: Proxy?,
    @NestedConfigurationProperty
    val ssl: SSL?,
    @DefaultValue
    val headers: List<HttpHeader>?,
    @DefaultValue
    @NestedConfigurationProperty
    val retry: Retry
) {
    data class Timeout(
        @DefaultValue("3s")
        val connection: Duration,
        @DefaultValue("3s")
        val read: Duration
    )

    data class Proxy(
        val host: String,
        val port: Int,
        val username: String?,
        val password: String?
    )

    data class SSL(
        val keystoreLocation: Resource?,
        val keystorePassword: String?,
        val keyPassword: String?,
        val truststoreLocation: Resource?,
        val truststorePassword: String?
    )

    data class HttpHeader(
        val name: String,
        val value: String
    )

    data class Retry(
        @DefaultValue("false")
        val enabled: Boolean,
        @DefaultValue("1s")
        val delay: Duration,
        @DefaultValue("3")
        val maxAttempts: Int,
        @DefaultValue
        val methods: Set<HttpMethod>,
        @DefaultValue("429, 500, 503")
        val statusCodes: Set<Int>
    )
}
