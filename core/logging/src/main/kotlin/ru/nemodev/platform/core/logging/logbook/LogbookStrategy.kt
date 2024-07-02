package ru.nemodev.platform.core.logging.logbook

import org.springframework.util.AntPathMatcher
import org.zalando.logbook.HttpRequest
import org.zalando.logbook.HttpResponse
import org.zalando.logbook.Strategy
import ru.nemodev.platform.core.logging.config.LoggingProperties

class LogbookStrategy(
    private val loggingProperties: LoggingProperties,
) : Strategy {

    companion object {
        private val ANT_PATH_MATCHER = AntPathMatcher().apply {
            setCachePatterns(true)
            setCaseSensitive(false)
        }
    }

    override fun process(request: HttpRequest): HttpRequest = when {
        loggingProperties.body.requestExcludeBodyPatterns.none { ANT_PATH_MATCHER.match(it, request.path) } -> request.withBody()
        else -> request.withoutBody()
    }

    override fun process(request: HttpRequest, response: HttpResponse): HttpResponse = when {
        loggingProperties.body.responseExcludeBodyPatterns.none { ANT_PATH_MATCHER.match(it, request.path) } -> response.withBody()
        else -> response.withoutBody()
    }
}