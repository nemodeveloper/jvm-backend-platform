package ru.nemodev.platform.core.integration.kafka.tracing

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import io.opentelemetry.semconv.SemanticAttributes
import org.springframework.kafka.support.micrometer.KafkaListenerObservation
import org.springframework.kafka.support.micrometer.KafkaListenerObservationConvention
import org.springframework.kafka.support.micrometer.KafkaRecordReceiverContext

class PlatformKafkaListenerObservationConvention(
    private val bootstrapServers: String
) : KafkaListenerObservationConvention {

    private val delegate = KafkaListenerObservation.DefaultKafkaListenerObservationConvention.INSTANCE

    override fun getLowCardinalityKeyValues(context: KafkaRecordReceiverContext): KeyValues {
        return delegate.getLowCardinalityKeyValues(context)
    }

    override fun getHighCardinalityKeyValues(context: KafkaRecordReceiverContext): KeyValues {
        return delegate.getHighCardinalityKeyValues(context)
            .and(
                KeyValue.of(
                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
                    context.carrier.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
                )
            )
    }

    /**
     * Костыль для проставления remoteServiceName вместо дефолтного
     * т.к нет возможности повлиять на создание KafkaRecordReceiverContext
     */
    override fun supportsContext(context: Observation.Context): Boolean {
        val support = delegate.supportsContext(context)
        if (support && context is KafkaRecordReceiverContext) {
            context.apply {
                remoteServiceName = KafkaTracingConst.REMOTE_SERVICE_NAME
                addLowCardinalityKeyValue(
                    KeyValue.of(
                        KafkaTracingConst.MESSAGING_CLUSTER_TAG_KEY,
                        bootstrapServers
                    )
                )
            }
        }

        return support
    }

    override fun getName() = delegate.name

    override fun getContextualName(context: KafkaRecordReceiverContext) = delegate.getContextualName(context)
}