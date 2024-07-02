package ru.nemodev.platform.core.async.extensions

import kotlinx.coroutines.*
import kotlinx.coroutines.slf4j.MDCContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

fun <T> doAsync(
    withCurrentMdcContext: Boolean = true,
    context: CoroutineContext? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    action: suspend () -> T
) = CoroutineScope(dispatcher)
    .async(
        getCoroutineContext(
            withCurrentMdcContext = withCurrentMdcContext,
            context = context
        )
    ) {
        action.invoke()
    }

fun <T> doLaunch(
    withCurrentMdcContext: Boolean = true,
    context: CoroutineContext? = null,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    action: suspend () -> T
) = CoroutineScope(dispatcher)
    .launch(
        getCoroutineContext(
            withCurrentMdcContext = withCurrentMdcContext,
            context = context
        )
    ) {
        action.invoke()
    }

private fun getCoroutineContext(
    withCurrentMdcContext: Boolean,
    context: CoroutineContext?
): CoroutineContext {
    return context.let {
        if (withCurrentMdcContext) {
            it?.plus(MDCContext()) ?: MDCContext()
        } else {
            it
        }
    } ?: EmptyCoroutineContext
}