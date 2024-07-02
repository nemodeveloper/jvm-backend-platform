package ru.nemodev.platform.core.logging.sl4j

import org.slf4j.Logger

interface Loggable {

    val logger: Logger get() = CustomLoggerFactory.getLogger(javaClass)

    fun logTrace(lazyMessage: () -> String) = logger.trace(lazyMessage = lazyMessage)

    fun logDebug(lazyMessage: () -> String) = logger.debug(lazyMessage = lazyMessage)
    fun logDebug(throwable: Throwable) = logger.debug(throwable.message, throwable)
    fun logDebug(throwable: Throwable, lazyMessage: () -> String) = logger.debug(throwable = throwable, lazyMessage = lazyMessage)

    fun logInfo(lazyMessage: () -> String) = logger.info(lazyMessage = lazyMessage)

    fun logWarn(lazyMessage: () -> String) = logger.warn(lazyMessage = lazyMessage)
    fun logWarn(t: Throwable) = logger.warn(t.message, t)
    fun logWarn(throwable: Throwable, lazyMessage: () -> String) = logger.warn(throwable = throwable, lazyMessage = lazyMessage)

    fun logError(lazyMessage: () -> String) = logger.error(lazyMessage = lazyMessage)
    fun logError(throwable: Throwable) = logger.error(throwable.message, throwable)
    fun logError(throwable: Throwable, lazyMessage: () -> String) = logger.error(throwable = throwable, lazyMessage = lazyMessage)

}