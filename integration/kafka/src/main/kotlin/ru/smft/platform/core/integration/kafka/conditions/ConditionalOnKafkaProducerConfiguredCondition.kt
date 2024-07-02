package ru.smft.platform.core.integration.kafka.conditions

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ConditionalOnKafkaProducerConfiguredCondition: Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {

        val attrs: Map<String, String> = metadata
            .getAnnotationAttributes(ConditionalOnKafkaProducerConfigured::class.java.name)
            ?.map { e -> e.key to e.value.toString() }
            ?.toMap()
            .takeUnless { it.isNullOrEmpty() }
            ?: return false

        val prefix = attrs["kafkaPrefix"]
            .takeUnless { it.isNullOrEmpty() }
            ?: throw IllegalArgumentException(
                "Illegal ConditionalOnKafkaProducerConfigured condition configuration: empty kafkaPrefix"
            )

        val producerKey = attrs["producerKey"]
            .takeUnless { it.isNullOrEmpty() }
            ?: throw IllegalArgumentException(
                "Illegal ConditionalOnKafkaProducerConfigured condition configuration: empty producerKey"
            )

        return context.environment.getProperty("$prefix.producers[$producerKey].enabled") == "true"
    }
}
