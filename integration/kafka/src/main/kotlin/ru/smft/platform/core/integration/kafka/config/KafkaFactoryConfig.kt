package ru.smft.platform.core.integration.kafka.config

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Bean
import reactor.kafka.receiver.MicrometerConsumerListener
import reactor.kafka.sender.MicrometerProducerListener
import ru.smft.platform.core.integration.kafka.consumer.SmartKafkaConsumer
import ru.smft.platform.core.integration.kafka.factory.KafkaFactory
import ru.smft.platform.core.integration.kafka.factory.KafkaFactoryImpl
import ru.smft.platform.core.integration.kafka.factory.SmartKafkaFactory
import ru.smft.platform.core.integration.kafka.factory.SmartKafkaFactoryImpl
import ru.smft.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.smft.platform.core.integration.kafka.logging.KafkaMessageLoggerImpl
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListener
import ru.smft.platform.core.logging.mdc.MdcService

@AutoConfiguration
class KafkaFactoryConfig {

    @Bean
    fun kafkaFactory(
        objectMapper: ObjectMapper,
        kafkaMessageLogger: KafkaMessageLogger,
        mdcService: MdcService,
        meterRegistry: MeterRegistry,
        observationRegistry: ObservationRegistry
    ): KafkaFactory = KafkaFactoryImpl(
        objectMapper,
        kafkaMessageLogger,
        mdcService,
        MicrometerProducerListener(meterRegistry),
        MicrometerConsumerListener(meterRegistry),
        observationRegistry
    )

    @Bean
    fun smartKafkaFactory(
        consumerMetricListener: ConsumerMetricListener,
        mdcService: MdcService,
        kafkaFactory: KafkaFactory,
        observationRegistry: ObservationRegistry
    ): SmartKafkaFactory = SmartKafkaFactoryImpl(
        consumerMetricListener,
        mdcService,
        kafkaFactory,
        observationRegistry
    )

    @Bean
    fun kafkaMessageLogger(objectMapper: ObjectMapper): KafkaMessageLogger
        = KafkaMessageLoggerImpl(objectMapper)

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