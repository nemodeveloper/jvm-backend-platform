package ru.nemodev.platform.core.integration.kafka.tracing

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import io.micrometer.observation.Observation
import io.opentelemetry.semconv.SemanticAttributes
import org.springframework.kafka.support.micrometer.KafkaRecordSenderContext
import org.springframework.kafka.support.micrometer.KafkaTemplateObservation
import org.springframework.kafka.support.micrometer.KafkaTemplateObservationConvention

class PlatformKafkaTemplateObservationConvention(
    private val bootstrapServers: String
) : KafkaTemplateObservationConvention {

    private val delegate = KafkaTemplateObservation.DefaultKafkaTemplateObservationConvention.INSTANCE

    override fun getHighCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
        return delegate.getHighCardinalityKeyValues(context)
            .and(
                KeyValue.of(
                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
                    context.carrier?.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
                )
            )
    }

    override fun getLowCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
        return delegate.getLowCardinalityKeyValues(context)
    }

    /**
     * Костыль для проставления remoteServiceName вместо дефолтного
     * т.к из SendSubscriber нет возможности повлиять на создание KafkaRecordSenderContext
     */
    override fun supportsContext(context: Observation.Context): Boolean {
        val support = delegate.supportsContext(context)
        if (support && context is KafkaRecordSenderContext) {
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

    override fun getContextualName(context: KafkaRecordSenderContext) = delegate.getContextualName(context)
}