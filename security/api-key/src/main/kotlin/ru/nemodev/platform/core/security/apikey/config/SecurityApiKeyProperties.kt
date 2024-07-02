package ru.nemodev.platform.core.security.apikey.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.http.HttpMethod

@ConfigurationProperties("platform.security.api-key")
data class SecurityApiKeyProperties(
    @DefaultValue("true")
    val enabled: Boolean,
    val key: String,
    @DefaultValue
    val authPaths: List<AuthPathProperties>,
    @DefaultValue
    @NestedConfigurationProperty
    val cors: CorsProperties
) {

    data class AuthPathProperties(
        @DefaultValue
        val methods: Set<HttpMethod>?,
        val path: String
    )

    data class CorsProperties(
        @DefaultValue("false")
        val enabled: Boolean
    )
}
