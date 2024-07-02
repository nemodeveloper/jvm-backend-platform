package ru.smft.platform.core.integration.kafka.producer

import kotlinx.coroutines.reactor.awaitSingle
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeaders
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import reactor.kafka.sender.SenderResult

interface SmartKafkaProducer<T: Any> {
    suspend fun send(key: String, value: T, headers: RecordHeaders? = null): SenderResult<Void>
    suspend fun send(value: T): SenderResult<Void>

    fun getTopic(): String
    fun getProducer(): ReactiveKafkaProducerTemplate<String, T>
}

class SmartKafkaProducerImpl<T: Any>(
    private val topic: String,
    private val producer: ReactiveKafkaProducerTemplate<String, T>
) : SmartKafkaProducer<T> {
    override suspend fun send(key: String, value: T, headers: RecordHeaders?): SenderResult<Void> {
        if (headers == null) {
            return producer.send(topic, key, value).awaitSingle()!!
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
        ).awaitSingle()!!
    }

    override suspend fun send(value: T) = producer.send(topic, value).awaitSingle()!!

    override fun getTopic() = topic
    override fun getProducer() = producer
}
