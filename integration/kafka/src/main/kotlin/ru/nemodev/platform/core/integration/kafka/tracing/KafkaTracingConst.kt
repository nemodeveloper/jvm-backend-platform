package ru.nemodev.platform.core.integration.kafka.tracing

object KafkaTracingConst {
    const val REMOTE_SERVICE_NAME = "Kafka"

    const val MESSAGING_BASE_TAG_KEY = "messaging.kafka"
    const val MESSAGING_CLUSTER_TAG_KEY = "${MESSAGING_BASE_TAG_KEY}.cluster"

    const val NULL_VALUE = "null"
}