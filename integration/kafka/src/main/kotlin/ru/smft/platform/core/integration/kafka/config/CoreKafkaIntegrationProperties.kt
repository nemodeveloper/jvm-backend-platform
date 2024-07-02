package ru.smft.platform.core.integration.kafka.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import java.time.Duration

@ConfigurationProperties(prefix = "smftp.core.integration.kafka")
data class CoreKafkaIntegrationProperties(
    @DefaultValue
    @NestedConfigurationProperty
    val metrics: KafkaMetric
) {
    data class KafkaMetric(
        @DefaultValue
        @NestedConfigurationProperty
        val consumer: KafkaConsumerMetric
    ) {
        data class KafkaConsumerMetric(
            @DefaultValue("true")
            val enabled: Boolean,
            @DefaultValue("120s")
            val initialRefreshDelay: Duration,
            @DefaultValue("30s")
            val refreshDelay: Duration
        )
    }
}
