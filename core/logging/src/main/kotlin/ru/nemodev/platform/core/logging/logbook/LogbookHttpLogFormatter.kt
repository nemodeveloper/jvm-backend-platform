package ru.nemodev.platform.core.logging.logbook

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.POJONode
import com.fasterxml.jackson.databind.util.RawValue
import org.springframework.http.MediaType
import org.zalando.logbook.*
import ru.nemodev.platform.core.logging.config.LoggingProperties
import ru.nemodev.platform.core.logging.sl4j.Loggable

class LogbookHttpLogFormatter(
    private val objectMapper: ObjectMapper,
    private val properties: LoggingProperties
): HttpLogFormatter {

    companion object : Loggable {
        private const val MESSAGE_TYPE = "HTTP"
    }

    override fun format(precorrelation: Precorrelation, request: HttpRequest): String {
        return formatLogMessage(
            objectMapper.createObjectNode().apply {
                put("type", MESSAGE_TYPE)
                put("direction", "request")
                put("correlationId", precorrelation.id)
                put("origin", request.origin.name.lowercase())
                put("remote", request.remote)
                put("scheme", request.scheme)
                put("protocol", request.protocolVersion)
                put("host", request.host)
                put("port", request.port.map { it.toString() }.orElse(""))
                put("path", request.path)
                put("query", request.query)
                put("contentType", request.contentType)
                put("method", request.method)
                put("uri", request.requestUri)
                putIfAbsent("headers", getHeaders(request.headers))
                putIfAbsent("body", getBody(request))
            }
        )
    }

    override fun format(correlation: Correlation, response: HttpResponse): String {
        return formatLogMessage(
            objectMapper.createObjectNode().apply {
                put("type", MESSAGE_TYPE)
                put("direction", "response")
                put("correlationId", correlation.id)
                put("origin", response.origin.name.lowercase())
                put("duration", correlation.duration.toMillis())
                put("status", response.status)
                putIfAbsent("headers", getHeaders(response.headers))
                putIfAbsent("body", getBody(response))
            }
        )
    }

    private fun formatLogMessage(logMessage: JsonNode): String {
        return if (properties.body.format.isJsonPretty()) {
            "\n${logMessage.toPrettyString()}"
        } else {
            logMessage.toString()
        }
    }

    private fun getHeaders(headers: HttpHeaders): ObjectNode {
        return objectMapper.createObjectNode().apply {
            headers.forEach { header ->
                put(header.key.toString(), header.value.toString())
            }
        }
    }

    private fun getBody(httpMessage: HttpMessage): JsonNode {
        if (httpMessage.body.isEmpty()) {
            return NullNode.getInstance()
        }

        val message = httpMessage.body.toString(Charsets.UTF_8)

        if (MediaType.APPLICATION_JSON_VALUE != httpMessage.contentType
            || !properties.body.format.isJsonPretty()
        ) {
            return POJONode(RawValue(message))
        }

        return try {
            objectMapper.createParser(message).readValueAsTree() as JsonNode
        } catch (e: Exception) {
            logError(e) { "Ошибка парсинга http body в json формат" }
            return POJONode(RawValue(message))
        }
    }
}
