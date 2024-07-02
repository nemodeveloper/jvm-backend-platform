package ru.smft.platform.core.integration.kafka.config

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListener
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListenerImpl
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListenerMock

@AutoConfiguration
@EnableConfigurationProperties(CoreKafkaIntegrationProperties::class)
class KafkaMetricConfig {

    @Bean
    @ConditionalOnProperty("smft.core.integration.kafka.metrics.consumer.enabled", havingValue = "true", matchIfMissing = true)
    fun consumerMetricListener(
        coreKafkaIntegrationProperties: CoreKafkaIntegrationProperties,
        meterRegistry: MeterRegistry
    ): ConsumerMetricListener = ConsumerMetricListenerImpl(coreKafkaIntegrationProperties, meterRegistry)

    @Bean
    @ConditionalOnMissingBean(ConsumerMetricListener::class)
    fun consumerMetricListenerMock(): ConsumerMetricListener = ConsumerMetricListenerMock()
}