package ru.smft.platform.core.integration.kafka.consumer

class ConcurrentSmartKafkaConsumerImpl<T>(
    private val smartKafkaConsumers: List<SmartKafkaConsumer<T>>
) : SmartKafkaConsumer<T> {
    override fun startConsumeMessages() {
        smartKafkaConsumers.forEach { it.startConsumeMessages() }
    }
}