package ru.nemodev.platform.core.integration.kafka.producer

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult

interface SmartKafkaProducer<T: Any> {

    fun getTopic(): String

    fun getProducer(): KafkaTemplate<String, T>

    fun send(key: String, value: T, headers: RecordHeaders? = null): SendResult<String, T>

    fun send(value: T): SendResult<String, T>
}

class SmartKafkaProducerImpl<T: Any>(
    private val topic: String,
    private val producer: KafkaTemplate<String, T>
) : SmartKafkaProducer<T> {

    override fun getTopic() = topic

    override fun getProducer() = producer

    override fun send(key: String, value: T, headers: RecordHeaders?): SendResult<String, T> {
        if (headers == null) {
            return producer.send(topic, key, value).get()
        }

        return producer.send(
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
        producer.send(topic, value).get()
}
