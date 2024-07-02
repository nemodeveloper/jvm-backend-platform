package ru.nemodev.platform.core.exception.critical

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.exception.AppException
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField

/**
 * Исключение, которое сигнализирует что в бизнес логике была допущена критическая ошибка
 * Или при работе сервисе случилась непредвиденная ситуация
 * Такую ошибку нельзя исправить клиентом, например произошла ошибка интеграции или неожиданное NPE
 * Такая ошибка транслируется как http status 5**
 */
open class CriticalException(
    errorCode: ErrorCode,
    errorFields: List<ErrorField>? = null,
    httpStatus: HttpStatusCode = HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()),
    message: String? = null,
    cause: Throwable? = null
) : AppException(
        errorCode = errorCode,
        errorFields = errorFields,
        httpStatus = httpStatus,
        message = message,
        cause = cause
)