package ru.nemodev.platform.core.async.executor

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Исполнение корутин в Dispatchers.Unconfined
 * Подходит для выполнения в случае если вам нужно выполнить корутину в текущем потоке обработки
 * Например для обработки сообщений из kafka или jms очередей
 */
class UnconfinedCoroutineExecutor(
    private val observationRegistry: ObservationRegistry
) : CoroutineExecutor {

    override val coroutineDispatcher: CoroutineDispatcher
        get() = Dispatchers.Unconfined

    override val observationRegistryContext: CoroutineContext
        get() = observationRegistry.asContextElement()
}