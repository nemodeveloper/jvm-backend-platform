package ru.nemodev.platform.core.integration.kafka.serialaizer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer
import ru.nemodev.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.nemodev.platform.core.logging.sl4j.Loggable

class LoggingSerializer<T>(
    private val delegate: Serializer<T>,
    private val isKey: Boolean,
    private val kafkaMessageLogger: KafkaMessageLogger,
    private val loggingPrettyEnabled: Boolean
) : Serializer<T> by delegate {

    companion object : Loggable {
        private const val KAFKA_KEY_INTERNAL_HEADER = "X-KafkaMessageInternal-Key"
    }

    override fun serialize(topic: String, headers: Headers?, data: T?): ByteArray? {
        return delegate.serialize(topic, headers, data).also {
            try {
                if (kafkaMessageLogger.enabled()) {
                    when {
                        isKey -> if (it != null) headers?.add(KAFKA_KEY_INTERNAL_HEADER, it)
                        else -> {
                            val restoredKey = headers?.lastHeader(KAFKA_KEY_INTERNAL_HEADER)?.value()?.toString(Charsets.UTF_8).orEmpty()
                            headers?.remove(KAFKA_KEY_INTERNAL_HEADER)
                            kafkaMessageLogger.logMessage(
                                loggingPrettyEnabled = loggingPrettyEnabled,
                                direction = "producer", // TODO сделать енум
                                topic = topic,
                                headers = headers
                                    ?.associate { h -> h.key() to h.value().toString(Charsets.UTF_8) }
                                    ?.filter { header -> header.key != KAFKA_KEY_INTERNAL_HEADER }
                                    .orEmpty(),
                                key = restoredKey,
                                message = it?.toString(Charsets.UTF_8).orEmpty()
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                logger.error("Ошибка логирования kafka message при отправке в topic = $topic", e)
            }
        }
    }
}