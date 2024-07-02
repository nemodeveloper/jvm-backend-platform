package ru.smft.platform.core.integration.kafka.exception

/**
 * Базовое исключение которое может случиться при обработке сообщения
 */
abstract class KafkaException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)

/**
 * Исключение, сообщение которого не может быть никак обработано, но его нужно закомитить в kafka
 */
class CommitKafkaMessageException(message: String? = null, cause: Throwable? = null) : KafkaException(message, cause)