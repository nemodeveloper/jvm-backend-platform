package ru.nemodev.platform.core.security.oauth2resource.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.http.HttpMethod

@ConfigurationProperties("platform.security.oauth2-resource")
data class SecurityOAuth2ResourceProperties(
    @DefaultValue("true")
    val enabled: Boolean,
    @DefaultValue
    @NestedConfigurationProperty
    val rsaKey: RsaKey,
    @DefaultValue
    val authPaths: List<AuthPath>,
) {
    data class RsaKey(
        val publicKey: String
    )

    data class AuthPath(
        val methods: Set<HttpMethod>?,
        val path: String,
        val roles: Set<String>?
    )
}
