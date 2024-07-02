package ru.smft.platform.core.integration.kafka.deserializer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.MDC
import ru.smft.platform.core.integration.kafka.deserializer.KeyDeserializer.Companion.KAFKA_KEY_INTERNAL_HEADER
import ru.smft.platform.core.integration.kafka.logging.KafkaMessageLogger
import ru.smft.platform.core.logging.mdc.MdcService
import ru.smft.platform.core.logging.sl4j.Loggable

class LoggingDeserializer<T>(
    private val delegate: Deserializer<T>,
    private val kafkaMessageLogger: KafkaMessageLogger,
    private val mdcService: MdcService,
    private val loggingPrettyEnabled: Boolean
) : Deserializer<T> by delegate {

    companion object : Loggable

    override fun deserialize(topic: String, headers: Headers?, data: ByteArray?): T? {
        try {
            mdcService.fillMdcFromHeaders(
                url = "kafka-topic/$topic",
                method = "KAFKA_CONSUMER",
                headerGetter = { headers?.lastHeader(it)?.value()?.toString(Charsets.UTF_8) },
            )
            if (kafkaMessageLogger.enabled()) {
                val restoredKey = headers?.lastHeader(KAFKA_KEY_INTERNAL_HEADER)?.value()?.toString(Charsets.UTF_8).orEmpty()
                kafkaMessageLogger.logMessage(
                    loggingPrettyEnabled = loggingPrettyEnabled,
                    direction = "consumer",
                    topic = topic,
                    headers = headers
                        ?.associate { h -> h.key() to h.value().toString(Charsets.UTF_8) }
                        ?.filter { it.key != KAFKA_KEY_INTERNAL_HEADER }
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
        finally {
            MDC.clear()
        }

        return delegate.deserialize(topic, headers, data)
    }
}