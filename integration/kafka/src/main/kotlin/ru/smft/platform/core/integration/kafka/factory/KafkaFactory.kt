package ru.smft.platform.core.integration.kafka.factory

import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.observation.ObservationRegistry
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.receiver.MicrometerConsumerListener
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.MicrometerProducerListener
import reactor.kafka.sender.SenderOptions
import ru.smft.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.smft.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.smft.platform.core.integration.kafka.deserializer.KeyDeserializer
import ru.smft.platform.core.integration.kafka.deserializer.LoggingDeserializer
import ru.smft.platform.core.integration.kafka.deserializer.SmartJsonDeserializer
import ru.smft.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.smft.platform.core.integration.kafka.serialaizer.LoggingSerializer
import ru.smft.platform.core.integration.kafka.serialaizer.MdcHeadersSerializer
import ru.smft.platform.core.integration.kafka.tracing.CustomKafkaSenderObservationConvention
import ru.smft.platform.core.logging.mdc.MdcService
import ru.smft.platform.core.logging.sl4j.Loggable

/**
 * Фабрика для создания kafka producer/consumer
 */
interface KafkaFactory {

    /**
     * Создать producer для отправки сообщений
     *
     * В качестве сериалазйера используется JsonSerializer
     * Который автоматически проставляет тип объекта в заголовок __TypeId__
     *
     * @param key ключ producer в списке producers
     * @param properties настройки для kafka producer
     */
    fun <T> createProducer(key: String, properties: KafkaIntegrationProperties, serializer: Serializer<T>? = null): ReactiveKafkaProducerTemplate<String, T>

    /**
     * Создать consumer для получения сообщений
     *
     * В качестве десериалайзера используется JsonDeserializer, который определяет тип объекта на основе заголовка __TypeId__, обернутый в SmartJsonDeserializer для кастомной обработки ошибок десериализации
     * Обычно данный заголовок автоматически проставляется при отправке сообщения с помощью JsonSerializer
     *
     * В случае если у вас нет заголовка __TypeId__ используйте параметры clazz или deserializer в параметрах метода
     *
     * @param key ключ consumer в списке consumers
     * @param properties настройки для kafka consumer
     * @param clazz класс объекта для десериалайзера если его нужно явно указать например если тип не приходит в заголовках сообщения
     * @param deserializer кастомный десериалайзер если не подходит JsonDeserializer по какой-то причине
     */
    fun <T> createConsumer(key: String, properties: KafkaIntegrationProperties,
                           clazz: Class<T>? = null,
                           deserializer: Deserializer<T>? = null
    ): ReactiveKafkaConsumerTemplate<String, DeserializeResult<T>>
}

class KafkaFactoryImpl(
    private val objectMapper: ObjectMapper,
    private val kafkaMessageLogger: KafkaMessageLogger,
    private val mdcService: MdcService,
    private val micrometerProducerListener: MicrometerProducerListener,
    private val micrometerConsumerListener: MicrometerConsumerListener,
    private val observationRegistry: ObservationRegistry
) : KafkaFactory {

    companion object : Loggable

    override fun <T> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>?
    ): ReactiveKafkaProducerTemplate<String, T> {

        val producer = properties.producers[key] ?:
        throw IllegalArgumentException("Для producer-key = $key не заданы настройки")

        val producerConfig = properties.broker
            .buildProducerProperties(null)
            .let { populateDefaultProducerProperties(it) }

        val keySerializer = StringSerializer()
            .let {
                if (producer.loggingEnabled) {
                    LoggingSerializer(it, true, kafkaMessageLogger, producer.loggingPrettyEnabled)
                }
                else { it }
            }.let {
                if (producer.fillHeadersFromMdc) {
                    MdcHeadersSerializer(it, mdcService)
                }
                else {
                    it
                }
            }

        val valueSerializer = (serializer ?: JsonSerializer(objectMapper)).let {
            if (producer.loggingEnabled) {
                LoggingSerializer(it, false, kafkaMessageLogger, producer.loggingPrettyEnabled)
            }
            else { it }
        }

        val kafkaOptions =
            SenderOptions.create<String, T>(producerConfig)
                .withKeySerializer(keySerializer)
                .withValueSerializer(valueSerializer)
                .let {
                    if (producer.metricsEnabled) {
                        it.producerListener(micrometerProducerListener)
                    } else {
                        it
                    }
                }.let {
                    if (producer.tracingEnabled) {
                        it.withObservation(
                            observationRegistry,
                            CustomKafkaSenderObservationConvention(
                                properties.broker.bootstrapServers.joinToString(separator = ",")
                            )
                        )
                    } else {
                        it
                    }
                }

        return ReactiveKafkaProducerTemplate(kafkaOptions)
    }

    override fun <T> createConsumer(
        key: String, properties: KafkaIntegrationProperties,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?
    ): ReactiveKafkaConsumerTemplate<String, DeserializeResult<T>> {

        val consumer = properties.consumers[key]
            ?: throw IllegalArgumentException("Для consumer-key = $key не заданы настройки")

        val consumerConfig: MutableMap<String, Any> = properties.broker.buildConsumerProperties(null).let {
            if (consumer.kafka != null) {
                it.putAll(consumer.kafka.buildProperties(null))
            }
            it
        }.let {
            populateDefaultConsumerProperties(it)
        }

        val keyDeserializer = KeyDeserializer(StringDeserializer())
        val valueDeserializer = getValueDeserializer(consumer, clazz, deserializer)

        val kafkaOptions =
            ReceiverOptions.create<String, DeserializeResult<T>>(consumerConfig)
                .withKeyDeserializer(keyDeserializer)
                .withValueDeserializer(valueDeserializer)
                .consumerProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                    consumerConfig[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG]?.toString().toBoolean()
                )
                .commitBatchSize(consumer.kafkaExtended.autoCommitBatchSize)
                .pollTimeout(consumer.kafkaExtended.maxPollTimeout)
                .let {
                    if (consumer.metricsEnabled) {
                        it.consumerListener(micrometerConsumerListener)
                    } else {
                        it
                    }
                }
                .subscription(listOf(consumer.topic))

        return ReactiveKafkaConsumerTemplate(kafkaOptions)
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

    private fun <T> getValueDeserializer(consumer: KafkaIntegrationProperties.ConsumerProperties,
                                         clazz: Class<T>? = null,
                                         deserializer: Deserializer<T>? = null
    ): Deserializer<DeserializeResult<T>> {
        val newDeserializer = deserializer ?:
        JsonDeserializer(clazz, objectMapper, clazz == null)
            .also { it.addTrustedPackages("*") }

        return SmartJsonDeserializer(newDeserializer).let {
            if (consumer.loggingEnabled) {
                LoggingDeserializer(it, kafkaMessageLogger, mdcService, consumer.loggingPrettyEnabled)
            }
            else { it }
        }
    }
}
