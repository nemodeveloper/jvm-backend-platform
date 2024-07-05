package ru.nemodev.platform.core.integration.kafka.factory

import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.consumer.KafkaMessageProcessor
import ru.nemodev.platform.core.integration.kafka.consumer.SmartKafkaConsumer
import ru.nemodev.platform.core.integration.kafka.consumer.SmartKafkaConsumerImpl
import ru.nemodev.platform.core.integration.kafka.producer.SmartKafkaProducer
import ru.nemodev.platform.core.integration.kafka.producer.SmartKafkaProducerImpl

interface SmartKafkaFactory {

    fun <T: Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>? = null
    ): SmartKafkaProducer<T>

    fun <T> createConsumer(
        consumerKey: String,
        properties: KafkaIntegrationProperties,
        messageProcessor: KafkaMessageProcessor<T>,
        clazz: Class<T>? = null,
        deserializer: Deserializer<T>? = null
    ): SmartKafkaConsumer<T>
}

class SmartKafkaFactoryImpl(
    private val kafkaFactory: KafkaFactory
) : SmartKafkaFactory {

    override fun <T : Any> createProducer(
        key: String,
        properties: KafkaIntegrationProperties,
        serializer: Serializer<T>?
    ): SmartKafkaProducer<T> {
        return SmartKafkaProducerImpl(
            topic = properties.producers[key]!!.topic,
            producer = kafkaFactory.createProducer(key, properties, serializer)
        )
    }

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

        return SmartKafkaConsumerImpl(
            consumerFactory = kafkaFactory.createConsumer(consumerKey, properties, clazz, deserializer),
            messageProcessor = messageProcessor,
            consumerProperties = consumerProperties
        )
    }
}
