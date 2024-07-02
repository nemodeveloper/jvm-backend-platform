package ru.nemodev.platform.core.logging.config

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.zalando.logbook.autoconfigure.LogbookProperties
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.logging.logback.LogbackInitService
import ru.nemodev.platform.core.logging.logback.LogstashEncoderFactory
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties::class)
@PropertySource(value = ["classpath:core-logging.yml"], factory = YamlPropertySourceFactory::class)
class LogbackConfig {

    @Bean
    fun logstashEncoderFactory(
        objectMapper: ObjectMapper,
        loggingProperties: LoggingProperties,
        environmentService: EnvironmentService,
    ) = LogstashEncoderFactory(
        objectMapper,
        loggingProperties,
        environmentService
    )

    @Bean
    fun logbackInitService(
        loggingProperties: LoggingProperties,
        logbookProperties: LogbookProperties,
        appenders: ObjectProvider<Appender<ILoggingEvent>>,
        logstashEncoderFactory: LogstashEncoderFactory,
        environmentService: EnvironmentService
    ): LogbackInitService = LogbackInitService(
        loggingProperties,
        logbookProperties,
        appenders,
        logstashEncoderFactory,
        environmentService
    )
}
