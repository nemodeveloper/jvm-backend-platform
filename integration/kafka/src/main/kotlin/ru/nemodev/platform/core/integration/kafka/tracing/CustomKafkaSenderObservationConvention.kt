package ru.nemodev.platform.core.integration.kafka.tracing

class CustomKafkaSenderObservationConvention(
    private val bootstrapServers: String
)
//) : KafkaTemplateObservation by DefaultKafkaTemplateObservationConvention.INSTANCE  {
//
//    override fun getHighCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
//        return super.getHighCardinalityKeyValues(context)
//            .and(
//                KeyValue.of(
//                    SemanticAttributes.MESSAGING_KAFKA_MESSAGE_KEY.key,
//                    context.carrier?.key()?.toString() ?: KafkaTracingConst.NULL_VALUE
//                )
//        )
//    }
//
//    override fun getLowCardinalityKeyValues(context: KafkaRecordSenderContext): KeyValues {
//        return super.getLowCardinalityKeyValues(context)
//            .and(
//                KeyValue.of(
//                    KafkaTracingConst.MESSAGING_KAFKA_TOPIC_TAG_KEY,
//                    context.carrier?.topic() ?: KafkaTracingConst.NULL_VALUE
//                )
//            )
//    }
//
//    /**
//     * Костыль для проставления remoteServiceName вместо дефолтного
//     * т.к из SendSubscriber нет возможности повлиять на создание KafkaRecordSenderContext
//     */
//    override fun supportsContext(context: Observation.Context): Boolean {
//        val support = super.supportsContext(context)
//        if (support && context is KafkaRecordSenderContext) {
//            context.apply {
//                remoteServiceName = KafkaTracingConst.REMOTE_SERVICE_NAME
//                addLowCardinalityKeyValue(
//                    KeyValue.of(
//                        KafkaTracingConst.MESSAGING_KAFKA_CLUSTER_TAG_KEY,
//                        bootstrapServers
//                    )
//                )
//            }
//        }
//
//        return support
//    }
//}