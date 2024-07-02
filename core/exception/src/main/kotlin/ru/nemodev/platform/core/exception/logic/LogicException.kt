package ru.nemodev.platform.core.exception.logic

import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.exception.AppException
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Исключение, которое сигнализирует что в бизнес логике была допущена ошибка
 * Такую ошибку можно исправить, например, путем ввода корректных данных
 * Такая ошибка транслируется как http status 4**
 */
open class LogicException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    httpStatus: HttpStatusCode,
    message: String? = null,
    cause: Throwable? = null
) : AppException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = httpStatus,
        message = message,
        cause = cause
)