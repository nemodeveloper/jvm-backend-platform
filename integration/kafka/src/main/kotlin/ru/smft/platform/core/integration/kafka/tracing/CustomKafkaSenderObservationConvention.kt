package ru.smft.platform.core.integration.kafka.tracing

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import io.opentelemetry.semconv.SemanticAttributes
import reactor.kafka.sender.observation.KafkaRecordSenderContext
import reactor.kafka.sender.observation.KafkaSenderObservation
import reactor.kafka.sender.observation.KafkaSenderObservationConvention

class CustomKafkaSenderObservationConvention(
    private val bootstrapServers: String
) : KafkaSenderObservationConvention by KafkaSenderObservation.DefaultKafkaSenderObservationConvention.INSTANCE  {

    override fun getHighCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
        return super.getHighCardinalityKeyValues(context)
            .and(
                KeyValue.of(
                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
                    context.carrier?.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
                )
        )
    }

    override fun getLowCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
        return super.getLowCardinalityKeyValues(context)
            .and(
                KeyValue.of(
                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_TAG_KEY,
                    context.carrier?.topic() ?: KafkaTracingConst.NULL_VALUE
                )
            )
    }

    /**
     * Костыль для проставления remoteServiceName вместо дефолтного
     * т.к из SendSubscriber нет возможности повлиять на создание KafkaRecordSenderContext
     */
    override fun supportsContext(context: Observation.Context): Boolean {
        val support = super.supportsContext(context)
        if (support && context is KafkaRecordSenderContext) {
            context.apply {
                remoteServiceName = KafkaTracingConst.REMOTE_SERVICE_NAME
                addLowCardinalityKeyValue(
                    KeyValue.of(
                        KafkaTracingConst.MESSAGING_KAFKA_CLUSTER_TAG_KEY,
                        bootstrapServers
                    )
                )
            }
        }

        return support
    }
}