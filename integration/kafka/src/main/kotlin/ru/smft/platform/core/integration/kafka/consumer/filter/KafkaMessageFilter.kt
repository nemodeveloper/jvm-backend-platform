package ru.smft.platform.core.integration.kafka.consumer.filter

/**
 * Фильтр kafka consumer сообщений
 */
interface KafkaMessageFilter<T> {
    fun filter(message: T): Boolean
}