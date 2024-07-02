package ru.nemodev.platform.core.exception.extensions

import org.springframework.http.HttpStatusCode
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.exception.critical.IntegrationCriticalException
import ru.nemodev.platform.core.exception.error.ErrorCode
import ru.nemodev.platform.core.exception.error.ErrorField
import ru.nemodev.platform.core.exception.logic.IntegrationLogicException

fun ErrorDtoRs.toIntegrationLogicException(
    service: String,
    statusCode: HttpStatusCode,
    message: String? = null
) = IntegrationLogicException(
    service = service,
    errorCode = ErrorCode.create(
        code = this.status.code,
        description = this.status.description
    ),
    errorFields = this.errors?.map {
        ErrorField.create(
            key = it.key,
            code = it.code,
            description = it.description
        )
    },
    httpStatus = statusCode,
    message = message
)

fun ErrorDtoRs.toIntegrationCriticalException(
    service: String,
    statusCode: HttpStatusCode,
    message: String? = null
) = IntegrationCriticalException(
    service = service,
    errorCode = ErrorCode.create(
        code = this.status.code,
        description = this.status.description
    ),
    errorFields = this.errors?.map {
        ErrorField.create(
            key = it.key,
            code = it.code,
            description = it.description
        )
    },
    httpStatus = statusCode,
    message = message
)