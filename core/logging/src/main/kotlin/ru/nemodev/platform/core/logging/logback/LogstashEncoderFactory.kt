package ru.nemodev.platform.core.logging.logback

import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.databind.ObjectMapper
import net.logstash.logback.composite.loggingevent.LoggingEventPatternJsonProvider
import net.logstash.logback.decorate.JsonGeneratorDecorator
import net.logstash.logback.encoder.LogstashEncoder
import net.logstash.logback.fieldnames.LogstashCommonFieldNames
import net.logstash.logback.fieldnames.LogstashFieldNames
import org.slf4j.LoggerFactory
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.logging.config.LoggingProperties
import ru.nemodev.platform.core.logging.extensions.getLoggingFormat

class LogstashEncoderFactory(
    private val objectMapper: ObjectMapper,
    private val loggingProperties: LoggingProperties,
    private val environmentService: EnvironmentService,
) {

    companion object {
        const val MESSAGE_PATTERN = """{"message": "#tryJson{%message}"}"""
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    }

    fun create(): LogstashEncoder = LogstashEncoder().apply {
        isIncludeContext = false
        isIncludeTags = false
        isIncludeMdc = true
        fieldNames = LogstashFieldNames().apply {
            message = LogstashCommonFieldNames.IGNORE_FIELD_INDICATOR // this suppresses default 'message'
            version = LogstashCommonFieldNames.IGNORE_FIELD_INDICATOR
            levelValue = LogstashCommonFieldNames.IGNORE_FIELD_INDICATOR
        }
        context = loggerContext
        if (environmentService.getLoggingFormat(loggingProperties.format).isJsonPretty()) {
            jsonGeneratorDecorator = JsonGeneratorDecorator { generator -> generator.useDefaultPrettyPrinter() }
        }
        providers.addProvider(LoggingEventPatternJsonProvider().apply {
            context = loggerContext
            setJsonFactory(objectMapper.factory)
            pattern = MESSAGE_PATTERN
        })
    }
}
