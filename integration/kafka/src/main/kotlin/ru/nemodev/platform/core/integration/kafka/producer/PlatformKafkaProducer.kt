package ru.nemodev.platform.core.integration.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult

interface PlatformKafkaProducer<T: Any> {

    fun getTopic(): String

    fun getKafkaTemplate(): KafkaTemplate<String, T>

    fun send(key: String, value: T, headers: RecordHeaders? = null): SendResult<String, T>

    fun send(value: T): SendResult<String, T>
}

class PlatformKafkaProducerImpl<T: Any>(
    private val topic: String,
    private val kafkaTemplate: KafkaTemplate<String, T>
) : PlatformKafkaProducer<T> {

    override fun getTopic() = topic

    override fun getKafkaTemplate() = kafkaTemplate

    override fun send(key: String, value: T, headers: RecordHeaders?): SendResult<String, T> {
        if (headers == null) {
            return kafkaTemplate.send(topic, key, value).get()
        }

        return kafkaTemplate.send(
            ProducerRecord(
                topic,
                null,
                null,
                key,
                value,
                headers
            )
        ).get()
    }

    override fun send(value: T): SendResult<String, T> =
        kafkaTemplate.send(topic, value).get()
}
