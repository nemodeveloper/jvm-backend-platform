package ru.nemodev.platform.core.integration.kafka.consumer

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.listener.AcknowledgingMessageListener
import org.springframework.kafka.support.Acknowledgment
import ru.nemodev.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.nemodev.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.nemodev.platform.core.logging.sl4j.Loggable

fun interface SmartKafkaConsumer<T> {
    fun startConsumeMessages()
}

fun interface KafkaMessageProcessor<T> {
    fun process(message: ConsumerRecord<String, DeserializeResult<T>>)
}

class SmartKafkaConsumerImpl<T>(
    private val consumerFactory: ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<T>>,
    private val messageProcessor: KafkaMessageProcessor<T>,
    private val consumerProperties: KafkaIntegrationProperties.ConsumerProperties
) : SmartKafkaConsumer<T> {

    companion object : Loggable

    private var isStartedConsumeMessages = false

    override fun startConsumeMessages() {
        check(!isStartedConsumeMessages) { "Обработка сообщений из топика ${consumerProperties.topic} уже запущена" }
        isStartedConsumeMessages = true

        consumerFactory
            .createContainer(consumerProperties.topic).apply {
                setupMessageListener(
                    object : AcknowledgingMessageListener<String, DeserializeResult<T>> {
                        override fun onMessage(
                            data: ConsumerRecord<String, DeserializeResult<T>>,
                            acknowledgment: Acknowledgment?
                        ) {
                            messageProcessor.process(data)
                            acknowledgment?.acknowledge()
                        }
                    }
                )
            }.start()
    }

//    private fun startConsumer(
//        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
//        consumerTemplate: ReactiveKafkaConsumerTemplate<String, DeserializeResult<T>>
//    ) {
//                    processMessage(receiverId, it)
//                    if (!autoCommit) {
//                        // Помечаем сообщение как обработанное и готовое к commit
//                        it.receiverOffset().acknowledge()
//                    }
//    }

//    private suspend fun processMessage(
//        receiverId: String,
//        record: ReceiverRecord<String, DeserializeResult<T>>
//    ) {
//        if (consumerProperties.tracingEnabled) {
//            val receiverObservation =
//                KafkaReceiverObservation.RECEIVER_OBSERVATION.start(
//                    null,
//                    KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
//                    {
//                        KafkaRecordReceiverContext(
//                            record,
//                            receiverId,
//                            bootstrapServers
//                        ).apply {
//                            remoteServiceName = KafkaTracingConst.REMOTE_SERVICE_NAME
//                            addLowCardinalityKeyValue(
//                                KeyValue.of(
//                                    KafkaTracingConst.MESSAGING_KAFKA_CLUSTER_TAG_KEY,
//                                    bootstrapServers
//                                )
//                            )
//                            addLowCardinalityKeyValue(
//                                KeyValue.of(
//                                    SemanticAttributes.MESSAGING_KAFKA_CONSUMER_GROUP.key,
//                                    kafkaProperties.consumer.groupId
//                                )
//                            )
//                            addLowCardinalityKeyValue(
//                                KeyValue.of(
//                                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_TAG_KEY,
//                                    record.topic()
//                                )
//                            )
//                            addLowCardinalityKeyValue(
//                                KeyValue.of(
//                                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_PARTITION_TAG_KEY,
//                                    record.partition().toString()
//                                )
//                            )
//                            addHighCardinalityKeyValue(
//                                KeyValue.of(
//                                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
//                                    record.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
//                                )
//                            )
//                            addHighCardinalityKeyValue(
//                                KeyValue.of(
//                                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_OFFSET.key,
//                                    record.offset().toString()
//                                )
//                            )
//                        }
//                    },
//                    observationRegistry
//                )
//        }
//    }
}
