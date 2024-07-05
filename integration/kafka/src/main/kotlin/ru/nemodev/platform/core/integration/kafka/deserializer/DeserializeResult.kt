package ru.nemodev.platform.core.integration.kafka.deserializer

import org.apache.kafka.common.header.Headers

sealed interface DeserializeResult<T> {
    data class Success<T>(val data: T): DeserializeResult<T>
    data class Failed<T>(val data: ByteArray, val headers: Headers?, val e: Throwable): DeserializeResult<T>
}