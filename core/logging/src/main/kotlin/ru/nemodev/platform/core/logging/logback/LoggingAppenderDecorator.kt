package ru.nemodev.platform.core.logging.logback

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import org.springframework.util.unit.DataSize
import org.zalando.logbook.autoconfigure.LogbookProperties
import ru.nemodev.platform.core.logging.config.LoggingProperties
import ru.nemodev.platform.core.logging.constant.Masking.MASKING_MARKER
import ru.nemodev.platform.core.logging.sl4j.Loggable
import kotlin.LazyThreadSafetyMode.NONE

class LoggingAppenderDecorator(
    private val delegate: Appender<ILoggingEvent>,
    private val loggingProperties: LoggingProperties,
    logbookProperties: LogbookProperties
) : Appender<ILoggingEvent> by delegate {

    private val masking = loggingProperties.masking.distinctBy { it.regex }
    private val replacement = logbookProperties.obfuscate.replacement

    override fun doAppend(event: ILoggingEvent) = delegate.doAppend(
        event
            .let {
                when {
                    masking.isNotEmpty() && (it.markerList.filter { marker -> marker == MASKING_MARKER }.size == 1) -> MaskingILoggingEvent(it, masking, replacement)
                    else -> it
                }
            }
            .let {
                when {
                    !loggingProperties.messageMaxSize.isNegative && it.formattedMessage.length > loggingProperties.messageMaxSize.toBytes() -> TrimmingILoggingEvent(it, loggingProperties.messageMaxSize)
                    else -> it
                }
            }
    )

    private class MaskingILoggingEvent(
        private val delegate: ILoggingEvent,
        private val masking: List<LoggingProperties.MaskingPattern>,
        private val replacement: String
    ) : ILoggingEvent by delegate {

        companion object : Loggable

        private val masked by lazy(mode = NONE) {
            masking.fold(delegate.formattedMessage) { acc, entity -> acc.replace(entity) }
        }

        private fun String.replace(maskingPattern: LoggingProperties.MaskingPattern): String = when {
            maskingPattern.valueRegex != null -> replace(maskingPattern.regex) {
                val matcher = maskingPattern.valueRegex.toPattern().matcher(it.value)
                try {
                    matcher.replaceAll(replacement)
                } catch (e: Exception) {
                    logError(e) { "Ошибка маскирования лога регулярным выражением при обработке $maskingPattern" }
                    throw e
                }
            }
            else -> {
                val matcher = maskingPattern.regex.toPattern().matcher(this)
                try {
                    matcher.replaceAll(replacement)
                } catch (e: Exception) {
                    logError(e) { "Ошибка маскирования лога регулярным выражением при обработке $maskingPattern" }
                    throw e
                }
            }
        }

        override fun getFormattedMessage(): String = masked
    }

    private class TrimmingILoggingEvent(
        private val delegate: ILoggingEvent,
        private val messageMaxSize: DataSize
    ) : ILoggingEvent by delegate {

        private val trimmed by lazy(mode = NONE) {
            delegate.formattedMessage.take(messageMaxSize.toBytes().toInt()) + "..."
        }

        override fun getFormattedMessage(): String = trimmed
    }
}
