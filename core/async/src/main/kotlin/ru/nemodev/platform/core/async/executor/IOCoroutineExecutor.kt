package ru.nemodev.platform.core.async.executor

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Исполнение корутин в Dispatchers.IO
 * Подходит для выполнения в шедулерах и фоновой обработке
 */
class IOCoroutineExecutor(
    private val observationRegistry: ObservationRegistry
) : CoroutineExecutor {

    override val coroutineDispatcher: CoroutineDispatcher
        get() = Dispatchers.IO

    override val observationRegistryContext: CoroutineContext
        get() = observationRegistry.asContextElement()
}