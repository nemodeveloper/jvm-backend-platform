package ru.smft.platform.core.integration.kafka.logging

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.util.RawValue
import ru.smft.platform.core.logging.formatter.BaseMessageLogger
import ru.smft.platform.core.logging.masking.Masking.MASKING_MARKER
import ru.smft.platform.core.logging.sl4j.Loggable
import ru.smft.platform.core.logging.sl4j.trace

interface KafkaMessageLogger : BaseMessageLogger {
    fun logMessage(
        loggingPrettyEnabled: Boolean,
        direction: String,
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

    override fun logMessage(
        loggingPrettyEnabled: Boolean,
        direction: String,
        topic: String,
        headers: Map<String, String>,
        key: String,
        message: String
    ) {
        logger.trace(MASKING_MARKER) {
            formatLogMessage(
                objectMapper.createObjectNode().apply {
                    put("type", MESSAGE_TYPE)
                    put("direction", direction)
                    put("topic", topic)
                    put("key", key)
                    putIfAbsent("headers", objectMapper.createArrayNode().apply {
                        headers.forEach { header ->
                            add(
                                objectMapper.createObjectNode().apply {
                                    put("name", header.key)
                                    put("value", header.value)
                                }
                            )
                        }
                    })
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

    override fun enabled() = logger.isTraceEnabled
    override fun getLogger() = logger
    override fun getMessageType() = MESSAGE_TYPE
}