package ru.nemodev.platform.core.logging.logbook

import org.zalando.logbook.BodyFilter
import org.zalando.logbook.ContentType
import org.zalando.logbook.autoconfigure.LogbookProperties
import org.zalando.logbook.json.JsonPathBodyFilters.jsonPath
import ru.nemodev.platform.core.logging.config.LoggingProperties

class LogbookJsonPathBodyFilter(
    loggingProperties: LoggingProperties,
    logbookProperties: LogbookProperties
) : BodyFilter {

    private val replacement = logbookProperties.obfuscate.replacement

    private val delegate: BodyFilter = loggingProperties
        .body.masking.jsonPaths
        .mapNotNull {
            val (jsonPath, pattern) = it
            with(jsonPath(jsonPath)) {
                when {
                    pattern == null -> replace(replacement)
                    else -> replace(pattern.toPattern(), replacement)
                }
            }
        }
        .reduceOrNull { acc, bodyFilter -> acc.tryMerge(bodyFilter) ?: acc }
        ?: BodyFilter { _, body -> body }

    override fun filter(contentType: String?, body: String): String =
        if (body.isBlank() || !ContentType.isJsonMediaType(contentType)) body
        else delegate.filter(contentType, body)
}
