package ru.nemodev.platform.core.async.executor

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.ObservationRegistry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Исполнение корутин в Dispatchers.Default
 * Подходит для выполнения в случае если вам нужен выделенный поток на обработку
 * В этом случае поток берется из Dispatchers.IO, но гарантируется что поток не делится своими ресурсами с другими корутинами
 */
class DefaultCoroutineExecutor(
    private val observationRegistry: ObservationRegistry
) : CoroutineExecutor {

    override val coroutineDispatcher: CoroutineDispatcher
        get() = Dispatchers.Default

    override val observationRegistryContext: CoroutineContext
        get() = observationRegistry.asContextElement()
}