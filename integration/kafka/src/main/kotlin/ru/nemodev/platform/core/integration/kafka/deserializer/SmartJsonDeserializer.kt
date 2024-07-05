package ru.nemodev.platform.core.integration.kafka.deserializer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.KeyDeserializer.Companion.KAFKA_KEY_INTERNAL_HEADER
import ru.nemodev.platform.core.logging.sl4j.Loggable

class SmartJsonDeserializer<T>(
    private val delegate: Deserializer<T>
) : Deserializer<DeserializeResult<T>> {

    companion object : Loggable

    override fun deserialize(topic: String, headers: Headers?, data: ByteArray): DeserializeResult<T> {
        return try {
            DeserializeResult.Success(delegate.deserialize(topic, headers, data))
        } catch (e: Throwable) {
            logError(e) {
                "Ошибка десериализиации сообщения kafka topic = $topic, message-key = ${headers?.lastHeader(KAFKA_KEY_INTERNAL_HEADER)}"
            }
            DeserializeResult.Failed(data, headers, e)
        }
        finally {
            headers?.remove(KAFKA_KEY_INTERNAL_HEADER)
        }
    }

    override fun deserialize(topic: String, data: ByteArray): DeserializeResult<T> {
        return deserialize(topic, null, data)
    }
}