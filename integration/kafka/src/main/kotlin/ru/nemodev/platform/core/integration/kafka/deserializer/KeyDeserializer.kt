package ru.nemodev.platform.core.integration.kafka.deserializer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer

class KeyDeserializer<T>(
    private val delegate: Deserializer<T>,
) : Deserializer<T> by delegate {

    companion object {
        const val KAFKA_MESSAGE_KEY_INTERNAL_HEADER = "X-KafkaMessageInternal-Key"
    }

    override fun deserialize(topic: String, headers: Headers?, data: ByteArray?): T? {
        if (data != null) headers?.add(KAFKA_MESSAGE_KEY_INTERNAL_HEADER, data)
        return super.deserialize(topic, headers, data)
    }
}