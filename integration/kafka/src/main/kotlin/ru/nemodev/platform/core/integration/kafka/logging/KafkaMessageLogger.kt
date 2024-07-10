package ru.nemodev.platform.core.integration.kafka.logging

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.util.RawValue
import ru.nemodev.platform.core.logging.constant.Masking.MASKING_MARKER
import ru.nemodev.platform.core.logging.sl4j.Loggable
import ru.nemodev.platform.core.logging.sl4j.trace

interface KafkaMessageLogger {

    enum class Direction(val type: String) {
        PRODUCER("producer"),
        CONSUMER("consumer")
    }

    fun enabled(): Boolean

    fun logMessage(
        loggingPrettyEnabled: Boolean,
        direction: Direction,
        topic: String,
        headers: Map<String, String>,
        key: String, message: String
    )
}

class KafkaMessageLoggerImpl(
    private val objectMapper: ObjectMapper
) : KafkaMessageLogger {

    companion object : Loggable {
        private const val MESSAGE_TYPE = "KAFKA"
    }

    override fun enabled() = logger.isTraceEnabled

    override fun logMessage(
        loggingPrettyEnabled: Boolean,
        direction: KafkaMessageLogger.Direction,
        topic: String,
        headers: Map<String, String>,
        key: String,
        message: String
    ) {
        logger.trace(MASKING_MARKER) {
            formatLogMessage(
                objectMapper.createObjectNode().apply {
                    put("type", MESSAGE_TYPE)
                    put("direction", direction.type)
                    put("topic", topic)
                    put("key", key)
                    putIfAbsent("headers", objectMapper.createObjectNode()
                        .apply {
                            headers.forEach { header ->
                                put(header.key, header.value)
                            }
                        }
                    )
                    putIfAbsent("message", getBody(message, loggingPrettyEnabled))
                },
                loggingPrettyEnabled
            )
        }
    }

    private fun formatLogMessage(logMessage: JsonNode, loggingPrettyEnabled: Boolean): String {
        return if (loggingPrettyEnabled) {
            "\n${logMessage.toPrettyString()}"
        } else {
            logMessage.toString()
        }
    }

    private fun getBody(message: String, loggingPrettyEnabled: Boolean): JsonNode {
        return if (loggingPrettyEnabled) {
            try {
                objectMapper.createParser(message).readValueAsTree() as JsonNode
            } catch (e: Exception) {
                logError(e) { "Ошибка парсинга kafka message в json формат" }
                return POJONode(RawValue(message))
            }
        } else {
            return POJONode(RawValue(message))
        }
    }
}