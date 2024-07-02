package ru.smft.platform.core.integration.kafka.consumer

import io.micrometer.common.KeyValue
import io.micrometer.observation.ObservationRegistry
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor
import io.opentelemetry.semconv.SemanticAttributes
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.apache.kafka.common.header.Headers
import org.slf4j.MDC
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverRecord
import reactor.kafka.receiver.observation.KafkaReceiverObservation
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext
import ru.smft.platform.core.extensions.getPrivateField
import ru.smft.platform.core.integration.kafka.config.KafkaIntegrationProperties
import ru.smft.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.smft.platform.core.integration.kafka.exception.CommitKafkaMessageException
import ru.smft.platform.core.integration.kafka.extension.toLog
import ru.smft.platform.core.integration.kafka.metric.ConsumerMetricListener
import ru.smft.platform.core.integration.kafka.tracing.KafkaTracingConst
import ru.smft.platform.core.logging.mdc.MdcService
import ru.smft.platform.core.logging.sl4j.Loggable

/**
 * Умный обработчик сообщений kafka
 */
fun interface SmartKafkaConsumer<T> {

    /**
     * Начать обработку сообщений
     * Метод следует вызывать по событию готовности приложения
     * Например при ApplicationReadyEvent
     */
    fun startConsumeMessages()
}

fun interface KafkaMessageProcessor<T> {

    /**
     * Обработчик в случае ошибок должен бросать одно из исключений наследника KafkaException
     * В случае если будет выброшен Throwable такое сообщение будет считаться не обработанным и не будет закомиченно в kafka процесс обработки сообщений будет остановлен
     */
    suspend fun process(message: ReceiverRecord<String, DeserializeResult<T>>)
}

/**
 * Дефолтная реализация умного обработчика
 * Если при обработке сообщения messageProcessor бросит исключение:
 * 1 - CommitKafkaMessageException сообщение будет закомичено, в лог будет записано сообщение с ошибкой
 * 2 - Throwable сообщение не будет закомичено, в лог будет записано сообщение с ошибкой
 */
class SmartKafkaConsumerImpl<T>(
    private val consumerMetricListener: ConsumerMetricListener,
    private val observationRegistry: ObservationRegistry,
    private val mdcService: MdcService,
    private val kafkaProperties: KafkaProperties,
    private val consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
    private val consumerTemplate: ReactiveKafkaConsumerTemplate<String, DeserializeResult<T>>,
    private val autoCommit: Boolean,
    private val messageProcessor: KafkaMessageProcessor<T>,
    private val threadNameSuffix: String?
) : SmartKafkaConsumer<T> {

    companion object : Loggable

    private var isStartedConsumeMessages = false
    private val bootstrapServers = kafkaProperties.bootstrapServers.joinToString(separator = ",")

    /**
     * Запуск метода расчитан на то что его будет вызывать 1 раз в однопоточном режиме
     * При повторной попытке вызова будет брошено исключение
     */
    override fun startConsumeMessages() {
        check(!isStartedConsumeMessages) { "Обработка сообщений из топика ${consumerProperties.topic} уже запущена" }
        isStartedConsumeMessages = true

        startConsumer(consumerProperties, consumerTemplate)
    }

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private fun startConsumer(
        consumerProperties: KafkaIntegrationProperties.ConsumerProperties,
        consumerTemplate: ReactiveKafkaConsumerTemplate<String, DeserializeResult<T>>
    ) {
        val threadName = "kafka-consumer-${consumerProperties.topic}".let {
            if (threadNameSuffix.isNullOrEmpty()) it
            else "$it-$threadNameSuffix"
        }

        val receiverId = consumerTemplate
            .getPrivateField<KafkaReceiver<String, T>>("kafkaReceiver")!!
            .getPrivateField<String>("receiverId")!!

        consumerTemplate
            .receive()
            .asFlow()
            .onEach {
                try {
                    fillMdcFromHeaders(consumerProperties.topic, it.headers())
                    processMessage(receiverId, it)
                    if (!autoCommit) {
                        // Помечаем сообщение как обработанное и готовое к commit
                        it.receiverOffset().acknowledge()
                    }
                } catch (e: CommitKafkaMessageException) {
                    logWarn(e) {
                        "Произошла ошибка обработки сообщения kafka, но ошибка не считается критической, сообщение считается успешно обработанным, ${it.toLog()}"
                    }
                } catch (e: Throwable) {
                    logError(e) {
                        "Произошла критическая ошибка обработки сообщения kafka, обработчик ошибки не задан, сообщение считается не обработанным, коммит сообщения в kafka не будет произведен, ${(it)}"
                    }
                    throw e
                } finally {
                    clearMdc()
                }
            }
            .retry {
                delay(consumerProperties.retryProcessDelay.toMillis())
                consumerMetricListener.onConsumerAdded(consumerProperties, kafkaProperties, consumerTemplate)
                true
            }
            .launchIn(CoroutineScope(newSingleThreadContext(threadName)))
            .also {
                consumerMetricListener.onConsumerAdded(consumerProperties, kafkaProperties, consumerTemplate)
                logInfo { "Старт чтения сообщений из kafka topic ${consumerProperties.topic}" }
            }
    }

    private suspend fun processMessage(
        receiverId: String,
        record: ReceiverRecord<String, DeserializeResult<T>>
    ) {
        if (consumerProperties.tracingEnabled) {
            val receiverObservation =
                KafkaReceiverObservation.RECEIVER_OBSERVATION.start(
                    null,
                    KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
                    {
                        KafkaRecordReceiverContext(
                            record,
                            receiverId,
                            bootstrapServers
                        ).apply {
                            remoteServiceName = KafkaTracingConst.REMOTE_SERVICE_NAME
                            addLowCardinalityKeyValue(
                                KeyValue.of(
                                    KafkaTracingConst.MESSAGING_KAFKA_CLUSTER_TAG_KEY,
                                    bootstrapServers
                                )
                            )
                            addLowCardinalityKeyValue(
                                KeyValue.of(
                                    SemanticAttributes.MESSAGING_KAFKA_CONSUMER_GROUP.key,
                                    kafkaProperties.consumer.groupId
                                )
                            )
                            addLowCardinalityKeyValue(
                                KeyValue.of(
                                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_TAG_KEY,
                                    record.topic()
                                )
                            )
                            addLowCardinalityKeyValue(
                                KeyValue.of(
                                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_PARTITION_TAG_KEY,
                                    record.partition().toString()
                                )
                            )
                            addHighCardinalityKeyValue(
                                KeyValue.of(
                                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
                                    record.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
                                )
                            )
                            addHighCardinalityKeyValue(
                                KeyValue.of(
                                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_OFFSET.key,
                                    record.offset().toString()
                                )
                            )
                        }
                    },
                    observationRegistry
                )

            mono {
                messageProcessor.process(record)
            }.doOnTerminate(receiverObservation::stop)
                .doOnError(receiverObservation::error)
                .contextWrite { context ->
                    context.put(ObservationThreadLocalAccessor.KEY, receiverObservation)
                }.awaitSingleOrNull()
        } else {
            messageProcessor.process(record)
        }
    }

    private fun fillMdcFromHeaders(topic: String, headers: Headers?) {
        if (consumerProperties.fillMdcFromHeaders && headers != null) {
            mdcService.fillMdcFromHeaders(
                url = "kafka-topic/$topic",
                method = "KAFKA_CONSUMER",
                headerGetter = { headers.lastHeader(it)?.value()?.toString(Charsets.UTF_8) },
            )
        }
    }

    private fun clearMdc() {
        if (consumerProperties.fillMdcFromHeaders) {
            MDC.clear()
        }
    }
}
