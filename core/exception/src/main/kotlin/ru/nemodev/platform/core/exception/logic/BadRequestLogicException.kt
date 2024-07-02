package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField


/**
 * Исключение некорректных данных или формата запроса
 */
open class BadRequestLogicException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = HttpStatus.BAD_REQUEST,
        message = message,
        cause = cause
)