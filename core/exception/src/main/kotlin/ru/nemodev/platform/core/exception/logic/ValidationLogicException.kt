package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField


/**
 * Исключение валидации данных введенных клиентом
 */
open class ValidationLogicException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = HttpStatus.UNPROCESSABLE_ENTITY,
        message = message,
        cause = cause
)