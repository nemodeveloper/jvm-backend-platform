package ru.nemodev.platform.core.integration.kafka.config

import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

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
        val count: Int,
        @DefaultValue("3000ms")
        val retryProcessDelay: Duration,
        @DefaultValue("true")
        val loggingEnabled: Boolean,
        @DefaultValue("true")
        val loggingPrettyEnabled: Boolean,
        @DefaultValue("true")
        val metricsEnabled: Boolean,
        @DefaultValue("true")
        val tracingEnabled: Boolean,
        @DefaultValue
        @NestedConfigurationProperty
        val kafkaExtended: KafkaConsumerExtended,
        @NestedConfigurationProperty
        val kafka: KafkaProperties.Consumer?
    ) {
        // TODO разобраться с настройками кафка как и куда сетить правильно
        data class KafkaConsumerExtended(
            @DefaultValue("50")
            val autoCommitBatchSize: Int,
            @DefaultValue("10000")
            val maxPollTimeout: Duration,
        )
    }
}
