package ru.smft.platform.core.integration.kafka.factory

import io.micrometer.observation.ObservationRegistry
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import ru.smft.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.smft.platform.core.integration.kafka.consumer.ConcurrentSmartKafkaConsumerImpl
import ru.smft.platform.core.integration.kafka.consumer.KafkaMessageProcessor
import ru.smft.platform.core.integration.kafka.consumer.SmartKafkaConsumer
import ru.smft.platform.core.integration.kafka.consumer.SmartKafkaConsumerImpl
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListener
import ru.smft.platform.core.integration.kafka.producer.SmartKafkaProducer
import ru.smft.platform.core.integration.kafka.producer.SmartKafkaProducerImpl
import ru.smft.platform.core.logging.mdc.MdcService

interface SmartKafkaFactory {

    fun <T> createConsumer(
        consumerKey: String,
        properties: KafkaIntegrationProperties,
        messageProcessor: KafkaMessageProcessor<T>,
        clazz: Class<T>? = null,
        deserializer: Deserializer<T>? = null
    ): SmartKafkaConsumer<T>

    fun <T: Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>? = null
    ): SmartKafkaProducer<T>
}

class SmartKafkaFactoryImpl(
    private val consumerMetricListener: ConsumerMetricListener,
    private val mdcService: MdcService,
    private val kafkaFactory: KafkaFactory,
    private val observationRegistry: ObservationRegistry
) : SmartKafkaFactory {

    override fun <T> createConsumer(
        consumerKey: String,
        properties: KafkaIntegrationProperties,
        messageProcessor: KafkaMessageProcessor<T>,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?
    ): SmartKafkaConsumer<T> {
        val consumerProperties = properties.consumers[consumerKey]
            ?: throw IllegalArgumentException("Для consumer-key = $consumerKey не заданы настройки")

        require(consumerProperties.count >= 1) {
            "Для consumer-key = $consumerKey настройка count должна быть >= 1"
        }

        return if (consumerProperties.count == 1) {
            createConsumer(consumerProperties, consumerKey, properties, messageProcessor, clazz, deserializer)
        } else {
            createConcurrentConsumer(consumerProperties, consumerKey, properties, messageProcessor, clazz, deserializer)
        }
    }

    override fun <T : Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>?
    ): SmartKafkaProducer<T> {
        val producer = kafkaFactory.createProducer(key, properties, serializer)
        return SmartKafkaProducerImpl(
            topic = properties.producers[key]!!.topic,
            producer = producer
        )
    }

    private fun <T> createConsumer(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        consumerKey: String,
        properties: KafkaIntegrationProperties,
        messageProcessor: KafkaMessageProcessor<T>,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?,
        threadNameSuffix: String? = null
    ): SmartKafkaConsumer<T> {
        val consumerTemplate = kafkaFactory.createConsumer(consumerKey, properties, clazz, deserializer)
        val springConsumerProperties = properties.broker.buildConsumerProperties(null).let {
            if (consumerProperties.kafka != null) {
                it.putAll(consumerProperties.kafka.buildProperties(null))
            }
            it
        }
        val autoCommit = springConsumerProperties[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG]?.toString().toBoolean()

        return SmartKafkaConsumerImpl(
            consumerMetricListener = consumerMetricListener,
            observationRegistry,
            mdcService = mdcService,
            kafkaProperties = properties.broker,
            consumerProperties = consumerProperties,
            consumerTemplate = consumerTemplate,
            autoCommit = autoCommit,
            messageProcessor = messageProcessor,
            threadNameSuffix = threadNameSuffix
        )
    }

    private fun <T> createConcurrentConsumer(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        consumerKey: String,
        properties: KafkaIntegrationProperties,
        messageProcessor: KafkaMessageProcessor<T>,
        clazz: Class<T>?,
        deserializer: Deserializer<T>?
    ): SmartKafkaConsumer<T> {
        val smartKafkaConsumers = generateSequence(0) { it + 1 }
            .take(consumerProperties.count)
            .map {
                createConsumer(
                    consumerProperties = consumerProperties,
                    consumerKey = consumerKey,
                    properties = properties,
                    messageProcessor = messageProcessor,
                    clazz = clazz,
                    deserializer = deserializer,
                    threadNameSuffix = it.toString()
                )
            }.toList()

        return ConcurrentSmartKafkaConsumerImpl(
            smartKafkaConsumers = smartKafkaConsumers
        )
    }
}
