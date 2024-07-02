package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Базовое исключение когда у пользователя нет доступа к какому-либо ресурсу
 * Такая ошибка транслируется как http status 403
 */
open class ForbiddenLogicalException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    message: String? = null,
    cause: Throwable? = null
) : LogicException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = HttpStatus.FORBIDDEN,
        message = message,
        cause = cause
)