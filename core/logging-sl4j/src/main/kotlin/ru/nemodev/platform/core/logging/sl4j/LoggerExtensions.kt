package ru.nemodev.platform.core.logging.sl4j

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker

inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

inline fun Logger.trace(marker: Marker? = null, lazyMessage: () -> String) {
    if (isTraceEnabled) {
        if (marker != null) trace(marker, lazyMessage()) else trace(lazyMessage())
    }
}

inline fun Logger.debug(marker: Marker? = null, lazyMessage: () -> String) {
    if (isDebugEnabled) {
        if (marker != null) debug(marker, lazyMessage()) else debug(lazyMessage())
    }
}

inline fun Logger.debug(marker: Marker? = null, throwable: Throwable, lazyMessage: () -> String) {
    if (isDebugEnabled) {
        if (marker != null) debug(marker, lazyMessage(), throwable) else debug(lazyMessage(), throwable)
    }
}

inline fun Logger.info(marker: Marker? = null, lazyMessage: () -> String) {
    if (isInfoEnabled) {
        if (marker != null) info(marker, lazyMessage()) else info(lazyMessage())
    }
}

inline fun Logger.warn(marker: Marker? = null, lazyMessage: () -> String) {
    if (isWarnEnabled) {
        if (marker != null) warn(marker, lazyMessage()) else warn(lazyMessage())
    }
}

inline fun Logger.warn(marker: Marker? = null, throwable: Throwable, lazyMessage: () -> String) {
    if (isWarnEnabled) {
        if (marker != null) warn(marker, lazyMessage(), throwable) else warn(lazyMessage(), throwable)
    }
}

inline fun Logger.error(marker: Marker? = null, lazyMessage: () -> String) {
    if (isErrorEnabled) {
        if (marker != null) error(marker, lazyMessage()) else error(lazyMessage())
    }
}

inline fun Logger.error(marker: Marker? = null, throwable: Throwable, lazyMessage: () -> String) {
    if (isErrorEnabled) {
        if (marker != null) error(marker, lazyMessage(), throwable) else error(lazyMessage(), throwable)
    }
}