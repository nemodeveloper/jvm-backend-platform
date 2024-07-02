package ru.smft.platform.core.integration.kafka.tracing

object KafkaTracingConst {
    const val REMOTE_SERVICE_NAME = "Apache Kafka"

    const val MESSAGING_KAFKA_BASE_TAG_KEY = "messaging.kafka"
    const val MESSAGING_KAFKA_CLUSTER_TAG_KEY = "${MESSAGING_KAFKA_BASE_TAG_KEY}.cluster"
    const val MESSAGING_KAFKA_TOPIC_TAG_KEY = "${MESSAGING_KAFKA_BASE_TAG_KEY}.topic"
    const val MESSAGING_KAFKA_TOPIC_PARTITION_TAG_KEY = "${MESSAGING_KAFKA_TOPIC_TAG_KEY}.partition"

    const val NULL_VALUE = "null"
}