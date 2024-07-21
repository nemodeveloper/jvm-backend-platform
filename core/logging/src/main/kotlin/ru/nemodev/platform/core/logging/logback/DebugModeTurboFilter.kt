package ru.nemodev.platform.core.logging.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.turbo.TurboFilter
import ch.qos.logback.core.spi.FilterReply
import ch.qos.logback.core.spi.FilterReply.ACCEPT
import ch.qos.logback.core.spi.FilterReply.NEUTRAL
import org.slf4j.MDC
import org.slf4j.Marker
import ru.nemodev.platform.core.api.headers.ApiHeaderNames

class DebugModeTurboFilter(
    private val logPackages: Set<String>
) : TurboFilter() {

    override fun decide(
        marker: Marker?,
        logger: Logger,
        level: Level?,
        format: String?,
        params: Array<out Any>?,
        t: Throwable?
    ): FilterReply = when {
        MDC.get(ApiHeaderNames.DEBUG_MODE) == "true" && logPackages.any { logger.name.startsWith(it) } -> ACCEPT
        else -> NEUTRAL
    }
}
