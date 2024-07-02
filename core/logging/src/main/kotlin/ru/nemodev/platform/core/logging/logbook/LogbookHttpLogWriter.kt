package ru.nemodev.platform.core.logging.logbook

import org.zalando.logbook.Correlation
import org.zalando.logbook.HttpLogWriter
import org.zalando.logbook.Precorrelation
import ru.nemodev.platform.core.logging.constant.Masking
import ru.nemodev.platform.core.logging.sl4j.Loggable
import ru.nemodev.platform.core.logging.sl4j.trace

class LogbookHttpLogWriter : HttpLogWriter {

    companion object : Loggable

    override fun isActive() = logger.isTraceEnabled

    override fun write(precorrelation: Precorrelation, request: String) {
        logger.trace(Masking.MASKING_MARKER) { request }
    }

    override fun write(correlation: Correlation, response: String) {
        logger.trace(Masking.MASKING_MARKER) { response }
    }
}