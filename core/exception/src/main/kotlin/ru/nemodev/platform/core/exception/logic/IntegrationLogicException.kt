package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Базовое исключение вызванное интеграцией со сторонним сервисом
 * Если сторонний сервис возвращает 4xx ошибку, то в httpStatus передается код ошибки
 */
open class IntegrationLogicException(
    val service: String,
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    httpStatus: HttpStatusCode,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = httpStatus,
        message = message,
        cause = cause
) {
    override val message = "Сервис вызвавший ошибку: $service, ${super.message}"
}