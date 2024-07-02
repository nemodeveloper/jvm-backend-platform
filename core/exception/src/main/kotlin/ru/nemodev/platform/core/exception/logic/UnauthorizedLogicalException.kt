package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Базовое исключение когда запрос не авторизован
 * Такая ошибка транслируется как http status 401
 */
open class UnauthorizedLogicalException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = HttpStatus.UNAUTHORIZED,
        message = message,
        cause = cause
)