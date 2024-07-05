package ru.nemodev.platform.core.integration.kafka.factory

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.nemodev.platform.core.integration.kafka.deserializer.KeyDeserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.LoggingDeserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.SmartJsonDeserializer
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.integration.kafka.serialaizer.LoggingSerializer
import ru.nemodev.platform.core.logging.sl4j.Loggable


interface KafkaFactory {

    fun <T> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>? = null
    ): KafkaTemplate<String, T>

    fun <T> createConsumer(
        key: String, properties: KafkaIntegrationProperties,
        clazz: Class<T>? = null,
        deserializer: Deserializer<T>? = null
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>>
}

class KafkaFactoryImpl(
    private val objectMapper: ObjectMapper,
    private val kafkaMessageLogger: KafkaMessageLogger,
//    private val micrometerProducerListener: MicrometerProducerListener<*, *>,
//    private val micrometerConsumerListener: MicrometerConsumerListener<*, *>
) : KafkaFactory {

    companion object : Loggable

    override fun <T> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>?
    ): KafkaTemplate<String, T> {

        val producer = properties.producers[key]
            ?: throw IllegalArgumentException("Для producer-key = $key не заданы настройки")

        val producerConfig = properties.broker
            .buildProducerProperties(null)
            .let { populateDefaultProducerProperties(it) }

        val keySerializer = StringSerializer()
            .let {
                if (producer.loggingEnabled) {
                    LoggingSerializer(it, true, kafkaMessageLogger, producer.loggingPrettyEnabled)
                } else { it }
            }

        val valueSerializer = (serializer ?: JsonSerializer(objectMapper)).let {
            if (producer.loggingEnabled) {
                LoggingSerializer(it, false, kafkaMessageLogger, producer.loggingPrettyEnabled)
            } else { it }
        }

        val factory = DefaultKafkaProducerFactory<String, T>(producerConfig).apply {
            setKeySerializer(keySerializer)
            setValueSerializer(valueSerializer)
//            if (producer.metricsEnabled) {
//                addListener(micrometerProducerListener)
//            }
        }

        return KafkaTemplate<String, T>(factory).apply {
            setMicrometerEnabled(producer.metricsEnabled)
            setObservationEnabled(producer.tracingEnabled)
        }
    }

    override fun <T> createConsumer(
        key: String, properties: KafkaIntegrationProperties,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>> {

        val consumerProperties = properties.consumers[key]
            ?: throw IllegalArgumentException("Для consumer-key = $key не заданы настройки")

        require(consumerProperties.count >= 1) {
            "Для consumer-key = $key настройка count должна быть >= 1"
        }

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

        return ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>>().apply {
            consumerFactory = DefaultKafkaConsumerFactory<String, DeserializeResult<T>>(consumerConfig).apply {
                setKeyDeserializer(KeyDeserializer(StringDeserializer()))
                setValueDeserializer(getValueDeserializer(consumerProperties, clazz, deserializer))
//            if (consumer.metricsEnabled) {
//                addListener(micrometerConsumerListener)
//            }
            }
            setConcurrency(consumerProperties.count)
            setContainerCustomizer {
                containerProperties.isMicrometerEnabled = consumerProperties.metricsEnabled
                containerProperties.isObservationEnabled = consumerProperties.tracingEnabled
            }
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
            properties[ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG] = 3000
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

        return SmartJsonDeserializer(newDeserializer).let {
            if (consumer.loggingEnabled) {
                LoggingDeserializer(it, kafkaMessageLogger, consumer.loggingPrettyEnabled)
            }
            else { it }
        }
    }
}
