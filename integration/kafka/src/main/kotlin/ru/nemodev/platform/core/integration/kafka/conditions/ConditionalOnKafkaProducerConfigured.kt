package ru.nemodev.platform.core.integration.kafka.conditions

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(ConditionalOnKafkaProducerConfiguredCondition::class)
annotation class ConditionalOnKafkaProducerConfigured(
    /** Путь до KafkaProperties */
    val kafkaPrefix: String,

    /** Ключ искомого продьюсера */
    val producerKey: String
)
