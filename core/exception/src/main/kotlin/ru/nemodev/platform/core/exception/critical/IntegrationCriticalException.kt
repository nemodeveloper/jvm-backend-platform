package ru.nemodev.platform.core.exception.critical

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Базовое исключение вызванное интеграцией со сторонним сервисом
 * Если сторонний сервис не доступен считаем это 503 HTTP ошибкой
 */
open class IntegrationCriticalException(
    val serviceId: String,
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    httpStatus: HttpStatusCode = HttpStatusCode.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()),
    cause: Throwable? = null
) : CriticalException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = httpStatus,
        message = message,
        cause = cause
) {
    override val message = "Сервис вызвавший ошибку: $serviceId, ${super.message}"
}