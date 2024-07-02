package ru.nemodev.platform.core.logging.extensions

import ru.nemodev.platform.core.environment.extensions.isK8S
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.logging.constant.LoggingFormat

object LoggingEnvironmentConst {
    const val LOGGING_FORMAT = "PLATFORM_CORE_LOGGING_FORMAT"
}

fun EnvironmentService.getLoggingFormat(propertyFormat: LoggingFormat): LoggingFormat {
    return if (getContourType().isK8S() || getCloudPlatform().isK8S()) {
        getEnvironment(LoggingEnvironmentConst.LOGGING_FORMAT)?.let { LoggingFormat.valueOf(it) }
            ?: LoggingFormat.JSON_COMPACT
    } else {
        propertyFormat
    }
}
