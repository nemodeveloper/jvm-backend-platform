package ru.smft.platform.core.integration.kafka.metric

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.coroutines.*
import kotlinx.coroutines.reactive.awaitFirst
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.TopicPartition
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.core.publisher.Mono
import ru.smft.platform.core.integration.kafka.config.CoreKafkaIntegrationProperties
import ru.smft.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.smft.platform.core.logging.sl4j.Loggable
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

fun interface ConsumerMetricListener {
    fun onConsumerAdded(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        kafkaProperties: KafkaProperties,
        reactiveConsumer: ReactiveKafkaConsumerTemplate<*, *>
    )
}

class ConsumerMetricListenerMock : ConsumerMetricListener {

    companion object : Loggable

    override fun onConsumerAdded(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        kafkaProperties: KafkaProperties,
        reactiveConsumer: ReactiveKafkaConsumerTemplate<*, *>
    ) {
        logWarn {
            "Метрики kafka consumer для topic = ${consumerProperties.topic} не будут добавлены в prometheus т.к отключена настройка smftp.core.integration.kafka.metrics.consumer.enabled"
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
class ConsumerMetricListenerImpl(
    private val coreKafkaIntegrationProperties: CoreKafkaIntegrationProperties,
    private val meterRegistry: MeterRegistry,
) : ConsumerMetricListener {

    data class ConsumerMetricObservable(
        val groupId: String,
        val topic: String,
        val bootstrapServers: List<String>
    )

    class ConsumerTopicOffset(
        val partition: String,
        var currentOffset: Long,
        var endOffset: Long,
        var lag: Long = abs(endOffset - currentOffset)
    )

    companion object : Loggable {
        const val KAFKA_CONSUMER_LAG_METRIC_NAME = "kafka_consumer_lag"
        const val KAFKA_CONSUMER_LAG_METRIC_DESCRIPTION = "Kafka consumer lag by group-id topic partition"

        const val KAFKA_CONSUMER_CURRENT_OFFSET_METRIC_NAME = "kafka_consumer_current_offset"
        const val KAFKA_CONSUMER_CURRENT_OFFSET_METRIC_DESCRIPTION = "Kafka consumer current offset by group-id topic partition"

        const val KAFKA_CONSUMER_END_OFFSET_METRIC_NAME = "kafka_consumer_end_offset"
        const val KAFKA_CONSUMER_END_OFFSET_METRIC_DESCRIPTION = "Kafka consumer end offset by group-id topic partition"

        const val KAFKA_CONSUMER_GROUP_ID_TAG_NAME = "groupId"
        const val KAFKA_CONSUMER_TOPIC_TAG_NAME = "topic"
        const val KAFKA_CONSUMER_PARTITION_TAG_NAME = "partition"
    }

    private val consumerTopicOffsetMap = ConcurrentHashMap<ConsumerMetricObservable, List<ConsumerTopicOffset>>()
    private val consumerAdminClientMap = ConcurrentHashMap<ConsumerMetricObservable, AdminClient>()
    private val consumerReactiveConsumerMap = ConcurrentHashMap<ConsumerMetricObservable, ReactiveKafkaConsumerTemplate<*, *>>()

    init {
        CoroutineScope(newSingleThreadContext("kafka-consumer-metrics")).launch {
            delay(coreKafkaIntegrationProperties.metrics.consumer.initialRefreshDelay.toMillis())
            while (true) {
                updateConsumerMetrics()
                delay(coreKafkaIntegrationProperties.metrics.consumer.refreshDelay.toMillis())
            }
        }
    }

    override fun onConsumerAdded(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        kafkaProperties: KafkaProperties,
        reactiveConsumer: ReactiveKafkaConsumerTemplate<*, *>
    ) {
        val groupId = consumerProperties.kafka?.groupId ?: kafkaProperties.consumer.groupId
        val consumerMetricObservable = ConsumerMetricObservable(
            groupId = groupId,
            topic = consumerProperties.topic,
            bootstrapServers = consumerProperties.kafka?.bootstrapServers
                ?: kafkaProperties.consumer.bootstrapServers
                ?: kafkaProperties.bootstrapServers
        )

        if (!consumerAdminClientMap.contains(consumerMetricObservable)) {
            val adminClient = consumerAdminClientMap.entries.firstOrNull {
                it.key.bootstrapServers == consumerMetricObservable.bootstrapServers
            }?.value ?: AdminClient.create(kafkaProperties.buildAdminProperties(null))

            consumerAdminClientMap[consumerMetricObservable] = adminClient
        }
        consumerReactiveConsumerMap[consumerMetricObservable] = reactiveConsumer
    }

    private suspend fun updateConsumerMetrics() {
        consumerAdminClientMap.forEach {
            val consumerMetricObservable = it.key
            try {
                val updatedConsumerMetrics = getConsumerMetrics(consumerMetricObservable)
                val currentConsumerOffsets = consumerTopicOffsetMap[consumerMetricObservable]

                if (currentConsumerOffsets == null) {
                    generateKafkaOffsetMetrics(consumerMetricObservable, updatedConsumerMetrics)
                } else {
                    val updatedConsumerTopicOffsetMap = updatedConsumerMetrics.associateBy { updatedConsumerTopicOffset -> updatedConsumerTopicOffset.partition }
                    currentConsumerOffsets.forEach { consumerTopicOffset ->
                        val updatedConsumerTopicOffset = updatedConsumerTopicOffsetMap[consumerTopicOffset.partition]!!
                        consumerTopicOffset.lag = updatedConsumerTopicOffset.lag
                        consumerTopicOffset.currentOffset = updatedConsumerTopicOffset.currentOffset
                        consumerTopicOffset.endOffset = updatedConsumerTopicOffset.endOffset
                    }
                }
            }
            catch (e: Exception) {
                logError(e) {
                    "Ошибка обновления метрик kafka consumer group-id = ${consumerMetricObservable.groupId} topic = ${consumerMetricObservable.topic}"
                }
            }
        }
    }

    private fun generateKafkaOffsetMetrics(consumerMetricObservable: ConsumerMetricObservable, consumerTopicOffsets: List<ConsumerTopicOffset>) {
        consumerTopicOffsetMap[consumerMetricObservable] = consumerTopicOffsets
        consumerTopicOffsets.forEach {
            Gauge.builder(KAFKA_CONSUMER_LAG_METRIC_NAME) { it.lag }
                .description(KAFKA_CONSUMER_LAG_METRIC_DESCRIPTION)
                .tag(KAFKA_CONSUMER_GROUP_ID_TAG_NAME, consumerMetricObservable.groupId)
                .tag(KAFKA_CONSUMER_TOPIC_TAG_NAME, consumerMetricObservable.topic)
                .tag(KAFKA_CONSUMER_PARTITION_TAG_NAME, it.partition)
                .register(meterRegistry)

            Gauge.builder(KAFKA_CONSUMER_CURRENT_OFFSET_METRIC_NAME) { it.currentOffset }
                .description(KAFKA_CONSUMER_CURRENT_OFFSET_METRIC_DESCRIPTION)
                .tag(KAFKA_CONSUMER_GROUP_ID_TAG_NAME, consumerMetricObservable.groupId)
                .tag(KAFKA_CONSUMER_TOPIC_TAG_NAME, consumerMetricObservable.topic)
                .tag(KAFKA_CONSUMER_PARTITION_TAG_NAME, it.partition)
                .register(meterRegistry)

            Gauge.builder(KAFKA_CONSUMER_END_OFFSET_METRIC_NAME) { it.endOffset }
                .description(KAFKA_CONSUMER_END_OFFSET_METRIC_DESCRIPTION)
                .tag(KAFKA_CONSUMER_GROUP_ID_TAG_NAME, consumerMetricObservable.groupId)
                .tag(KAFKA_CONSUMER_TOPIC_TAG_NAME, consumerMetricObservable.topic)
                .tag(KAFKA_CONSUMER_PARTITION_TAG_NAME, it.partition)
                .register(meterRegistry)
        }
    }

    private suspend fun getConsumerMetrics(consumerMetricObservable: ConsumerMetricObservable): List<ConsumerTopicOffset> {
        val consumerOffsets = getConsumerTopicOffsets(consumerMetricObservable)
        val producerOffsets = getProducerTopicOffsets(consumerMetricObservable, consumerOffsets)

        return computeTopicOffsets(consumerOffsets, producerOffsets)
    }

    private suspend fun getConsumerTopicOffsets(consumerMetricObservable: ConsumerMetricObservable): Map<TopicPartition, Long> {
        return Mono.fromCallable {
            consumerAdminClientMap[consumerMetricObservable]!!
                .listConsumerGroupOffsets(consumerMetricObservable.groupId)
                .partitionsToOffsetAndMetadata().get()
        }.map { offsets ->
            offsets.toList()
                .filter { it.first.topic() == consumerMetricObservable.topic } // TODO c 1 кластера можно получать сразу все но тогда усложниться логика в updateConsumerMetrics
                .associate { TopicPartition(it.first.topic(), it.first.partition()) to it.second.offset() }
        }.awaitFirst()
    }

    private suspend fun getProducerTopicOffsets(
        consumerMetricObservable: ConsumerMetricObservable,
        consumerOffsets: Map<TopicPartition, Long>
    ): Map<TopicPartition, Long> {
        return consumerReactiveConsumerMap[consumerMetricObservable]!!.endOffsets(
            *consumerOffsets.map { TopicPartition(it.key.topic(), it.key.partition()) }.toTypedArray()
        ).collectList().map { offsets -> offsets.associate { it.t1 to it.t2 } }.awaitFirst()
    }

    private fun computeTopicOffsets(
        consumerOffsets: Map<TopicPartition, Long>,
        producerOffsets: Map<TopicPartition, Long>
    ): List<ConsumerTopicOffset> {
        return consumerOffsets.map {
            val producerOffset = producerOffsets[it.key]!!
            val consumerOffset = consumerOffsets[it.key]!!

            ConsumerTopicOffset(
                partition = it.key.partition().toString(),
                endOffset = producerOffset,
                currentOffset = consumerOffset
            )
        }
    }
}
