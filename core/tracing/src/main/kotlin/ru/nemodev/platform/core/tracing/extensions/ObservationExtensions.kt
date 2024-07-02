package ru.nemodev.platform.core.tracing.extensions

import io.micrometer.observation.Observation
import io.micrometer.observation.Observation.Context
import io.micrometer.observation.ObservationConvention
import io.micrometer.observation.ObservationRegistry

fun ObservationRegistry.createNotStarted(name: String): Observation {
    return Observation.createNotStarted(name, this)
}

@Suppress("UNCHECKED_CAST")
fun <T : Context> ObservationRegistry.createNotStarted(
    observationConvention: ObservationConvention<T>,
    context: Context
): Observation {
    return Observation.createNotStarted(
        observationConvention,
        observationConvention,
        { context as T },
        this
    )
}

suspend fun <T> Observation.observeSuspend(action: suspend () -> T): T {
    start()
    return try {
        action.invoke()
    } catch (e: Throwable) {
        error(e)
        throw e
    } finally {
        stop()
    }
}