package ru.nemodev.platform.core.integration.kafka.factory

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerEndpoint
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.nemodev.platform.core.integration.kafka.deserializer.KeyDeserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.LoggingDeserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.PlatformJsonDeserializer
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.integration.kafka.producer.PlatformKafkaProducer
import ru.nemodev.platform.core.integration.kafka.producer.PlatformKafkaProducerImpl
import ru.nemodev.platform.core.integration.kafka.serialaizer.LoggingSerializer
import ru.nemodev.platform.core.integration.kafka.tracing.PlatformKafkaListenerObservationConvention
import ru.nemodev.platform.core.integration.kafka.tracing.PlatformKafkaTemplateObservationConvention
import ru.nemodev.platform.core.logging.sl4j.Loggable


interface PlatformKafkaFactory {

    fun <T> createDefaultKafkaProducerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>? = null
    ): DefaultKafkaProducerFactory<String, T>

    fun <T> createKafkaTemplate(
        key: String,
        properties: KafkaIntegrationProperties,
        defaultKafkaProducerFactory: DefaultKafkaProducerFactory<String, T>
    ): KafkaTemplate<String, T>

    fun <T: Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        kafkaTemplate: KafkaTemplate<String, T>
    ): PlatformKafkaProducer<T>

    fun <T> createDefaultKafkaConsumerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        clazz: Class<T>? = null,
        deserializer: Deserializer<T>? = null
    ): DefaultKafkaConsumerFactory<String, DeserializeResult<T>>

    fun <T> createConcurrentKafkaListenerContainerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        defaultKafkaConsumerFactory: DefaultKafkaConsumerFactory<String, DeserializeResult<T>>
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>>
}

class PlatformKafkaFactoryImpl(
    private val objectMapper: ObjectMapper,
    private val kafkaMessageLogger: KafkaMessageLogger
) : PlatformKafkaFactory {

    companion object : Loggable

    override fun <T> createDefaultKafkaProducerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>?
    ): DefaultKafkaProducerFactory<String, T> {

        val producerProperties = properties.producers[key]
            ?: throw IllegalArgumentException("Для producer-key = $key не заданы настройки")

        val producerConfig = properties.broker
            .buildProducerProperties(null)
            .let { populateDefaultProducerProperties(it) }

        val keySerializer = StringSerializer()
            .let {
                if (producerProperties.loggingEnabled) {
                    LoggingSerializer(it, true, kafkaMessageLogger, producerProperties.loggingPrettyEnabled)
                } else { it }
            }

        val valueSerializer = (serializer ?: JsonSerializer(objectMapper)).let {
            if (producerProperties.loggingEnabled) {
                LoggingSerializer(it, false, kafkaMessageLogger, producerProperties.loggingPrettyEnabled)
            } else { it }
        }

        return DefaultKafkaProducerFactory<String, T>(producerConfig).apply {
            setKeySerializer(keySerializer)
            setValueSerializer(valueSerializer)
        }
    }

    override fun <T> createKafkaTemplate(
        key: String,
        properties: KafkaIntegrationProperties,
        defaultKafkaProducerFactory: DefaultKafkaProducerFactory<String, T>
    ): KafkaTemplate<String, T> {
        val producerProperties = properties.producers[key]
            ?: throw IllegalArgumentException("Для producer-key = $key не заданы настройки")

        return KafkaTemplate<String, T>(defaultKafkaProducerFactory).apply {
            setMicrometerEnabled(producerProperties.metricsEnabled)
            setObservationEnabled(producerProperties.tracingEnabled)
            setObservationConvention(
                PlatformKafkaTemplateObservationConvention(
                    properties.broker.bootstrapServers.joinToString()
                )
            )
        }
    }

    override fun <T : Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        kafkaTemplate: KafkaTemplate<String, T>
    ): PlatformKafkaProducer<T> {
        return PlatformKafkaProducerImpl(
            topic = properties.producers[key]!!.topic,
            kafkaTemplate = kafkaTemplate
        )
    }

    override fun <T> createDefaultKafkaConsumerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?
    ): DefaultKafkaConsumerFactory<String, DeserializeResult<T>> {

        val consumerProperties = properties.consumers[key]
            ?: throw IllegalArgumentException("Для consumer-key = $key не заданы настройки")

        val consumerConfig: MutableMap<String, Any> = properties.broker
            .buildConsumerProperties(null)
            .let {
                if (consumerProperties.kafka != null) {
                    it.putAll(consumerProperties.kafka.buildProperties(null))
                }
                it
            }.let {
                populateDefaultConsumerProperties(it)
            }

        return DefaultKafkaConsumerFactory<String, DeserializeResult<T>>(consumerConfig).apply {
            setKeyDeserializer(KeyDeserializer(StringDeserializer()))
            setValueDeserializer(getValueDeserializer(consumerProperties, clazz, deserializer))
        }
    }

    override fun <T> createConcurrentKafkaListenerContainerFactory(
        key: String,
        properties: KafkaIntegrationProperties,
        defaultKafkaConsumerFactory: DefaultKafkaConsumerFactory<String, DeserializeResult<T>>
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>> {
        val consumerProperties = properties.consumers[key]
            ?: throw IllegalArgumentException("Для consumer-key = $key не заданы настройки")

        require(consumerProperties.concurrency >= 1) {
            "Для consumer-key = $key настройка count должна быть >= 1"
        }

        return object : ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>>() {
            override fun createContainerInstance(endpoint: KafkaListenerEndpoint): ConcurrentMessageListenerContainer<String, DeserializeResult<T>> {
                return ConcurrentMessageListenerContainer(defaultKafkaConsumerFactory, ContainerProperties(consumerProperties.topic))
            }
        }.apply {
            consumerFactory = defaultKafkaConsumerFactory
            setConcurrency(consumerProperties.concurrency)
            containerProperties.isMicrometerEnabled = consumerProperties.metricsEnabled
            containerProperties.isObservationEnabled = consumerProperties.tracingEnabled
            containerProperties.observationConvention = PlatformKafkaListenerObservationConvention(
                properties.broker.bootstrapServers.joinToString()
            )
        }
    }

    private fun populateDefaultProducerProperties(properties: MutableMap<String, Any>): MutableMap<String, Any> {
        if (properties[ProducerConfig.ACKS_CONFIG] == null) {
            properties[ProducerConfig.ACKS_CONFIG] = "all"
        }
        if (properties[ProducerConfig.RETRIES_CONFIG] == null) {
            properties[ProducerConfig.RETRIES_CONFIG] = 3
        }
        return properties
    }

    private fun populateDefaultConsumerProperties(properties: MutableMap<String, Any>): MutableMap<String, Any> {
        if (properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] == null) {
            properties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        }
        if (properties[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] == null) {
            properties[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] = 1000
        }
        if (properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] == null) {
            properties[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        }
        if (properties[ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG] == null) {
            properties[ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG] = "org.apache.kafka.clients.consumer.CooperativeStickyAssignor"
        }
        if (properties[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] == null) {
            properties[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 10000
        }
        if (properties[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] == null) {
            properties[ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG] = 3000 // 1/3 от SESSION_TIMEOUT_MS_CONFIG
        }
        if (properties[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] == null) {
            properties[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 300000 // 5 минут
        }
        if (properties[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] == null) {
            properties[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 100
        }
        if (properties[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] == null) {
            properties[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] = 10485760 // 10MB
        }

        return properties
    }

    private fun <T> getValueDeserializer(
        consumer: KafkaIntegrationProperties.ConsumerProperties,
        clazz: Class<T>? = null,
        deserializer: Deserializer<T>? = null
    ): Deserializer<DeserializeResult<T>> {
        val newDeserializer = deserializer ?:
        JsonDeserializer(clazz, objectMapper, clazz == null)
            .also { it.addTrustedPackages("*") }

        return PlatformJsonDeserializer(newDeserializer).let {
            if (consumer.loggingEnabled) {
                LoggingDeserializer(it, kafkaMessageLogger, consumer.loggingPrettyEnabled)
            }
            else { it }
        }
    }
}
