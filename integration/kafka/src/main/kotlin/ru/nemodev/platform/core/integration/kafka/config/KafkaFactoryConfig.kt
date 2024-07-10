package ru.nemodev.platform.core.integration.kafka.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.integration.kafka.factory.PlatformKafkaFactory
import ru.nemodev.platform.core.integration.kafka.factory.PlatformKafkaFactoryImpl
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLoggerImpl
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@PropertySource(value = ["classpath:core-kafka.yml"], factory = YamlPropertySourceFactory::class)
class KafkaFactoryConfig {

    @Bean
    fun kafkaMessageLogger(objectMapper: ObjectMapper): KafkaMessageLogger =
        KafkaMessageLoggerImpl(objectMapper)

    @Bean
    fun platformKafkaFactory(
        objectMapper: ObjectMapper,
        kafkaMessageLogger: KafkaMessageLogger,
        meterRegistry: MeterRegistry,
    ): PlatformKafkaFactory = PlatformKafkaFactoryImpl(
        objectMapper,
        kafkaMessageLogger
    )
}