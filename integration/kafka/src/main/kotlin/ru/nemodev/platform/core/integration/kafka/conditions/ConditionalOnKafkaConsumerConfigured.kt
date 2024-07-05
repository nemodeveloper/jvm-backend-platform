package ru.nemodev.platform.core.integration.kafka.conditions

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(ru.nemodev.platform.core.integration.kafka.conditions.ConditionalOnKafkaConsumerConfiguredCondition::class)
annotation class ConditionalOnKafkaConsumerConfigured(
    /** Путь до KafkaProperties */
    val kafkaPrefix: String,

    /** Ключ искомого консьюмера */
    val consumerKey: String
)
