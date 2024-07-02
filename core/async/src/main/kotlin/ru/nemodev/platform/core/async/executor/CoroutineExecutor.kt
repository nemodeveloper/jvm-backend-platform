package ru.nemodev.platform.core.async.executor

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import ru.nemodev.platform.core.async.extensions.doAsync
import ru.nemodev.platform.core.async.extensions.doLaunch
import kotlin.coroutines.CoroutineContext

interface CoroutineExecutor {

    val observationRegistryContext: CoroutineContext
    val coroutineDispatcher: CoroutineDispatcher

    fun <T> async(
        withCurrentMdcContext: Boolean = true,
        withObservationContext: Boolean = true,
        context: CoroutineContext? = null,
        action: suspend () -> T
    ): Deferred<T> = doAsync(
        withCurrentMdcContext = withCurrentMdcContext,
        context = getCoroutineContext(
            withObservationContext = withObservationContext,
            context = context
        ),
        dispatcher = coroutineDispatcher,
        action = action
    )

    fun <T> launch(
        withCurrentMdcContext: Boolean = true,
        withObservationContext: Boolean = true,
        context: CoroutineContext? = null,
        action: suspend () -> T
    ): Job = doLaunch(
        withCurrentMdcContext = withCurrentMdcContext,
        context = getCoroutineContext(
            withObservationContext = withObservationContext,
            context = context
        ),
        dispatcher = coroutineDispatcher,
        action = action
    )

    private fun getCoroutineContext(
        withObservationContext: Boolean,
        context: CoroutineContext?
    ): CoroutineContext? {
        return observationRegistryContext.let {
            if (withObservationContext) {
                context?.plus(it) ?: it
            } else {
                context
            }
        }
    }
}