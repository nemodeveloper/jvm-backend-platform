package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Базовое исключение когда не удалось найти ресурс
 * Такая ошибка транслируется как http status 404
 */
open class NotFoundLogicalException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = HttpStatus.NOT_FOUND,
        message = message,
        cause = cause
)