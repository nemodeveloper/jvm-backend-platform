package ru.nemodev.platform.core.async.config

import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import ru.nemodev.platform.core.async.executor.DefaultCoroutineExecutor
import ru.nemodev.platform.core.async.executor.IOCoroutineExecutor
import ru.nemodev.platform.core.async.executor.UnconfinedCoroutineExecutor

@AutoConfiguration
class AsyncConfig {

    @Bean
    fun ioCoroutineExecutor(
        observationRegistry: ObservationRegistry
    ) = IOCoroutineExecutor(observationRegistry)

    @Bean
    fun defaultCoroutineExecutor(
        observationRegistry: ObservationRegistry
    ) = DefaultCoroutineExecutor(observationRegistry)

    @Bean
    fun unconfinedCoroutineExecutor(
        observationRegistry: ObservationRegistry
    ) = UnconfinedCoroutineExecutor(observationRegistry)
}