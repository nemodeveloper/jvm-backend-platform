package ru.smft.platform.core.integration.kafka.extension

import reactor.kafka.receiver.ReceiverRecord

fun ReceiverRecord<*, *>.toLog(): String {
    return "kafka topic = ${topic()}, partition = ${partition()}, key = ${key()} offset = ${offset()}"
}