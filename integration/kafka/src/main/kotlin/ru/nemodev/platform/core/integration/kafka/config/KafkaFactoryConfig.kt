package ru.nemodev.platform.core.integration.kafka.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import ru.nemodev.platform.core.integration.kafka.consumer.SmartKafkaConsumer
import ru.nemodev.platform.core.integration.kafka.factory.KafkaFactory
import ru.nemodev.platform.core.integration.kafka.factory.KafkaFactoryImpl
import ru.nemodev.platform.core.integration.kafka.factory.SmartKafkaFactory
import ru.nemodev.platform.core.integration.kafka.factory.SmartKafkaFactoryImpl
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLoggerImpl

@AutoConfiguration
class KafkaFactoryConfig {

    @Bean
    fun kafkaFactory(
        objectMapper: ObjectMapper,
        kafkaMessageLogger: KafkaMessageLogger
    ): KafkaFactory = KafkaFactoryImpl(
        objectMapper,
        kafkaMessageLogger
    )

    @Bean
    fun smartKafkaFactory(
        kafkaFactory: KafkaFactory,
    ): SmartKafkaFactory = SmartKafkaFactoryImpl(kafkaFactory)

    @Bean
    fun kafkaMessageLogger(objectMapper: ObjectMapper): KafkaMessageLogger = KafkaMessageLoggerImpl(objectMapper)

    @Bean
    fun onStartUpSmartKafkaListener(
        smartKafkaListeners: List<SmartKafkaConsumer<out Any>>
    ): ApplicationListener<ApplicationReadyEvent> {
        return ApplicationListener<ApplicationReadyEvent> {
            smartKafkaListeners.forEach {
                it.startConsumeMessages()
            }
        }
    }
}