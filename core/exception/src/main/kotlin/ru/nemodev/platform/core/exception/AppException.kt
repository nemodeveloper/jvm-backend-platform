package ru.nemodev.platform.core.exception

import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField


abstract class AppException(
    val errorCode: ErrorCode,
    val errorFields: List<ErrorField>? = null,
    val httpStatus: HttpStatusCode,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(message, cause) {

    override val message = "$errorCode" +
            (if (errorFields.isNullOrEmpty()) "" else ", поля с ошибками: $errorFields)") +
            (if (message.isNullOrBlank()) "" else "\n${message}")

}

