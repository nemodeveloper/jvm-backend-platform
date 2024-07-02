package ru.nemodev.platform.core.exception.error

interface ErrorCode {
    fun getCode(): String
    fun getDescription(): String

    companion object {
        fun create(code: String, description: String): ErrorCode
            = ErrorCodeImpl(code = code, description = description)

        fun createBadRequest(description: String = "Некорректный запрос"): ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.BAD_REQUEST.name, description = description)

        fun createNotFound(description: String): ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.NOT_FOUND.name, description = description)

        fun createForbidden(description: String): ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.FORBIDDEN.name, description = description)

        fun createValidation(description: String): ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.VALIDATION.name, description = description)

        fun createServiceTimeout(description: String): ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.SERVICE_TIMEOUT.name, description = description)

        fun createServiceUnavailable(description: String) : ErrorCode
            = ErrorCodeImpl(code = BaseErrorCode.SERVICE_UNAVAILABLE.name, description = description)

        fun createUnauthorized(description: String) : ErrorCode
                = ErrorCodeImpl(code = BaseErrorCode.UNAUTHORIZED.name, description = description)
    }
}

enum class BaseErrorCode {
    // Logic errors
    BAD_REQUEST,
    NOT_FOUND,
    FORBIDDEN,
    VALIDATION,
    UNAUTHORIZED,
    INVALID_FIELD_FORMAT,
    FIELD_REQUIRED,

    // Critical errors
    SERVICE_TIMEOUT,
    SERVICE_UNAVAILABLE
}

data class ErrorCodeImpl(
    private val code: String,
    private val description: String
) : ErrorCode {
    override fun getCode() = code
    override fun getDescription() = description
    override fun toString() = "Ошибка: код = $code, описание = $description"
}