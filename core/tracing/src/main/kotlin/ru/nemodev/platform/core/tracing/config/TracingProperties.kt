package ru.nemodev.platform.core.tracing.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties(prefix = "platform.core.tracing")
data class TracingProperties(
    @DefaultValue("false")
    val springSecurityEnabled: Boolean,
    @DefaultValue("false")
    val baseApiEnabled: Boolean,
)
