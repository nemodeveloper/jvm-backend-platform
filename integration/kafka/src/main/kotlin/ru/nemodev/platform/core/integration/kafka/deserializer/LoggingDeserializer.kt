package ru.nemodev.platform.core.integration.kafka.deserializer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import ru.nemodev.platform.core.integration.kafka.deserializer.KeyDeserializer.Companion.KAFKA_MESSAGE_KEY_INTERNAL_HEADER
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.logging.sl4j.Loggable

class LoggingDeserializer<T>(
    private val delegate: Deserializer<T>,
    private val kafkaMessageLogger: KafkaMessageLogger,
    private val loggingPrettyEnabled: Boolean
) : Deserializer<T> by delegate {

    companion object : Loggable

    override fun deserialize(topic: String, headers: Headers?, data: ByteArray?): T? {
        try {
            if (kafkaMessageLogger.enabled()) {
                val restoredKey = headers?.lastHeader(KAFKA_MESSAGE_KEY_INTERNAL_HEADER)?.value()?.toString(Charsets.UTF_8).orEmpty()
                kafkaMessageLogger.logMessage(
                    loggingPrettyEnabled = loggingPrettyEnabled,
                    direction = KafkaMessageLogger.Direction.CONSUMER,
                    topic = topic,
                    headers = headers
                        ?.associate { h -> h.key() to h.value().toString(Charsets.UTF_8) }
                        ?.filter { it.key != KAFKA_MESSAGE_KEY_INTERNAL_HEADER }
                        .orEmpty(),
                    key = restoredKey,
                    message = data?.toString(Charsets.UTF_8).orEmpty()
                )
            }
        } catch (e: Exception) {
            logError(e) {
                "Ошибка логирования kafka message topic = $topic"
            }
        }

        return delegate.deserialize(topic, headers, data)
    }
}