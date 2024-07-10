package ru.nemodev.platform.core.integration.kafka.config

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

data class KafkaIntegrationProperties(
    @DefaultValue
    val producers: Map<String, ProducerProperties>,
    @DefaultValue
    val consumers: Map<String, ConsumerProperties>,
    @DefaultValue
    @NestedConfigurationProperty
    val broker: KafkaProperties
) {
    data class ProducerProperties(
        @DefaultValue("true")
        val enabled: Boolean,
        val topic: String,
        @DefaultValue("true")
        val loggingEnabled: Boolean,
        @DefaultValue("true")
        val loggingPrettyEnabled: Boolean,
        @DefaultValue("true")
        val metricsEnabled: Boolean,
        @DefaultValue("true")
        val tracingEnabled: Boolean
    )

    data class ConsumerProperties(
        @DefaultValue("true")
        val enabled: Boolean,
        val topic: String,
        @DefaultValue("1")
        val concurrency: Int,
        @DefaultValue("true")
        val loggingEnabled: Boolean,
        @DefaultValue("true")
        val loggingPrettyEnabled: Boolean,
        @DefaultValue("true")
        val metricsEnabled: Boolean,
        @DefaultValue("true")
        val tracingEnabled: Boolean,
        @NestedConfigurationProperty
        val kafka: KafkaProperties.Consumer?
    )
}
