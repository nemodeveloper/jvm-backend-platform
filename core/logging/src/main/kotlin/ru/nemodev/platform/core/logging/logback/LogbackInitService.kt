package ru.nemodev.platform.core.logging.logback

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.OutputStreamAppender
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.zalando.logbook.autoconfigure.LogbookProperties
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.logging.config.LoggingProperties
import ru.nemodev.platform.core.logging.constant.LoggingFormat
import ru.nemodev.platform.core.logging.extensions.getLoggingFormat
import ru.nemodev.platform.core.logging.sl4j.info
import ru.nemodev.platform.core.logging.sl4j.warn

class LogbackInitService(
    private val loggingProperties: LoggingProperties,
    private val logbookProperties: LogbookProperties,
    private val appenders: ObjectProvider<Appender<ILoggingEvent>>,
    private val logstashEncoderFactory: LogstashEncoderFactory,
    private val environmentService: EnvironmentService,
) {
    @PostConstruct
    fun init() {
        val loggingFormat: LoggingFormat = environmentService.getLoggingFormat(loggingProperties.format)
        val jsonEncoder = logstashEncoderFactory.create()
        (LoggerFactory.getILoggerFactory() as LoggerContext).also { loggerContext ->
            loggerContext.addTurboFilter(DebugModeTurboFilter(loggingProperties.logPackages))
            loggerContext.loggerList.forEach { logger ->
                logger.iteratorForAppenders().forEach { appender ->
                    logger.detachAppender(appender.name)
                    if (appender is OutputStreamAppender && !loggingFormat.isPlain()) {
                        appender.encoder = jsonEncoder.also { it.start() }
                    }
                    logger.addAppender(
                        LoggingAppenderDecorator(
                            delegate = appender,
                            loggingProperties = loggingProperties,
                            logbookProperties = logbookProperties
                        )
                    )
                }
                if (logger.name == Logger.ROOT_LOGGER_NAME) {
                    if (!loggingProperties.consoleEnabled) {
                        logger.warn { "Отключен вывод логов в консоль platform.core.logging.console-enabled = false" }
                        logger.detachAppender("CONSOLE")
                    }
                    appenders.forEach { appender ->
                        logger.addAppender(
                            LoggingAppenderDecorator(
                                delegate = appender,
                                loggingProperties = loggingProperties,
                                logbookProperties = logbookProperties
                            ).apply {
                                context = loggerContext
                                start()
                                logger.info { "Проинициализирован логгер ${appender.name}" }
                            }
                        )
                    }
                }
            }
        }
    }
}
