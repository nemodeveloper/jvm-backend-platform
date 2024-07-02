package ru.smft.platform.core.integration.kafka.conditions

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ConditionalOnKafkaConsumerConfiguredCondition: Condition {
    override fun matches(
        context: ConditionContext,
        metadata: AnnotatedTypeMetadata
    ): Boolean {

        val attrs: Map<String, String> = metadata
            .getAnnotationAttributes(ConditionalOnKafkaConsumerConfigured::class.java.name)
            ?.map { e -> e.key to e.value.toString() }
            ?.toMap()
            .takeUnless { it.isNullOrEmpty() }
            ?: return false

        val prefix = attrs["kafkaPrefix"]
            .takeUnless { it.isNullOrEmpty() }
            ?: throw IllegalArgumentException(
                "Illegal ConditionalOnKafkaConsumerConfigured condition configuration: empty kafkaPrefix"
            )

        val consumerKey = attrs["consumerKey"]
            .takeUnless { it.isNullOrEmpty() }
            ?: throw IllegalArgumentException(
                "Illegal ConditionalOnKafkaConsumerConfigured condition configuration: empty consumerKey"
            )

        return context.environment.getProperty("$prefix.consumers[$consumerKey].enabled") == "true"
    }
}
