package ru.nemodev.platform.core.api.exception.handler

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import io.micrometer.observation.ObservationRegistry
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.hibernate.validator.internal.engine.path.PathImpl
import org.springframework.core.io.buffer.DataBufferLimitException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.MethodNotAllowedException
import org.springframework.web.server.ServerWebInputException
import org.springframework.web.server.UnsupportedMediaTypeStatusException
import org.springframework.web.servlet.resource.NoResourceFoundException
import ru.nemodev.platform.core.api.dto.error.ErrorDtoRs
import ru.nemodev.platform.core.api.dto.error.ErrorFieldDtoRs
import ru.nemodev.platform.core.api.dto.error.StatusDtoRs
import ru.nemodev.platform.core.api.exception.handler.config.ApiExceptionHandlerProperties
import ru.nemodev.platform.core.api.exception.handler.const.ValidatorConst.PATTERN_LIST_DELIMITER
import ru.nemodev.platform.core.api.exception.handler.const.ValidatorConst.PATTERN_LIST_FIELD_ERROR_CODE
import ru.nemodev.platform.core.api.exception.handler.const.ValidatorConst.WEB_EXCHANGE_BIND_EXCEPTION_DELIMITER
import ru.nemodev.platform.core.exception.critical.CriticalException
import ru.nemodev.platform.core.exception.error.BaseErrorCode
import ru.nemodev.platform.core.exception.logic.LogicException
import ru.nemodev.platform.core.extensions.isNotNullOrEmpty
import ru.nemodev.platform.core.extensions.nullIfEmpty
import ru.nemodev.platform.core.logging.sl4j.Loggable

@RestControllerAdvice
class ApiExceptionHandler(
    private val properties: ApiExceptionHandlerProperties,
    private val observationRegistry: ObservationRegistry
) {

    // Обработка 4** ошибок

    @ExceptionHandler(ConstraintViolationException::class)
    fun onConstraintValidationException(
        exception: ConstraintViolationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        val errors = exception.constraintViolations.map { error ->
            ErrorFieldDtoRs(
                key = (error.propertyPath as PathImpl).leafNode.toString(),
                code = properties.getExternalErrorFieldCode(error.constraintDescriptor.annotation.annotationClass.simpleName ?: FIELD_BAD_VALUE_CODE),
                description = error.message
            )
        }

        return ResponseEntity.unprocessableEntity().body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.UNPROCESSABLE_ENTITY.name,
                    description = UNPROCESSABLE_ENTITY_REQUEST_MESSAGE
                ),
                errors = errors
            )
        )
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleBadRequestValidationExceptions(
        exception: WebExchangeBindException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        val errors = exception.bindingResult.allErrors.map { error: ObjectError ->
            val fieldError = error as? FieldError

            if (fieldError == null) {
                ErrorFieldDtoRs(
                    key = extractFieldFromWebExchangeBindException(error.defaultMessage!!),
                    code = properties.getExternalErrorFieldCode("NotEmpty"),
                    description = error.defaultMessage ?: "Некорректное значение"
                )
            } else {
                // Обработка исключения вызванного кастомной аннотацией @PatternList
                // ИЗМЕНЕНИЕ ЭТОГО БЛОКА ЗАТРАГИВАЕТ class PatternListValidator !!!!!
                if (error.code == PATTERN_LIST_FIELD_ERROR_CODE) {
                    val fieldName = error.defaultMessage!!

                    val index = fieldName.substring(0, fieldName.indexOf(PATTERN_LIST_DELIMITER) + 1)
                    val description = fieldName.substring(fieldName.indexOf(PATTERN_LIST_DELIMITER) + 1, fieldName.length)

                    ErrorFieldDtoRs(
                        key = "${fieldError.field}$index",
                        code = properties.getExternalErrorFieldCode(error.code!!),
                        description = description
                    )
                } else {
                    ErrorFieldDtoRs(
                        key = fieldError.field,
                        code = properties.getExternalErrorFieldCode(error.code ?: FIELD_BAD_VALUE_CODE),
                        description = error.defaultMessage ?: "Некорректное значение"
                    )
                }
            }
        }

        return ResponseEntity.unprocessableEntity().body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.UNPROCESSABLE_ENTITY.name,
                    description = UNPROCESSABLE_ENTITY_REQUEST_MESSAGE
                ),
                errors = errors
            )
        )
    }

    @ExceptionHandler(ServerWebInputException::class)
    fun handleBadRequestException(
        exception: ServerWebInputException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        val rootCause = exception.rootCause
        var errorMessage = ""
        var field: String? = null
        var description: String? = null
        var code = ""
        when (rootCause) {
            is InvalidFormatException -> {
                field = extractInvalidFieldPath(rootCause)
                code = BaseErrorCode.INVALID_FIELD_FORMAT.name
                errorMessage = "Поле $field должно иметь тип ${rootCause.targetType}"
            }
            is MismatchedInputException -> {
                field = extractInvalidFieldPath(rootCause)
                code = BaseErrorCode.FIELD_REQUIRED.name
                errorMessage = "Отсутствует обязательное поле $field"
            }
            else -> {
                description = "$BAD_REQUEST_MESSAGE: ${exception.reason}"
            }
        }

        return ResponseEntity.badRequest().body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.BAD_REQUEST.name,
                    description = description ?: BAD_REQUEST_MESSAGE
                ),
                errors = if (field == null) null else
                    listOf(
                        ErrorFieldDtoRs(
                            key = field,
                            code = code,
                            description = errorMessage
                        )
                    )
                )
            )
    }

    @ExceptionHandler(MethodNotAllowedException::class)
    fun handleBadRequestException(
        exception: MethodNotAllowedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.METHOD_NOT_ALLOWED.name,
                    description = "HTTP метод не поддерживается для данного URL: ${exception.reason}"
                )
            ),
            HttpStatus.METHOD_NOT_ALLOWED
        )
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException::class)
    fun handleBadRequestException(
        exception: UnsupportedMediaTypeStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.UNSUPPORTED_MEDIA_TYPE.name,
                    description = "HTTP media type не поддерживается для данного URL: ${exception.reason}"
                )
            ),
            HttpStatus.METHOD_NOT_ALLOWED
        )
    }

    @ExceptionHandler(DataBufferLimitException::class)
    fun handleDataBufferLimitException(
        exception: DataBufferLimitException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.UNPROCESSABLE_ENTITY.name,
                    description = "Превышен размер загружаемого файла"
                )
            ),
            HttpStatus.UNPROCESSABLE_ENTITY
        )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(
        exception: NoResourceFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.NOT_FOUND.name,
                    description = "Ресурс не найден"
                )
            ),
            HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(LogicException::class)
    fun handleLogicException(
        exception: LogicException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity.status(exception.httpStatus).body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = exception.errorCode.getCode(),
                    description = exception.errorCode.getDescription()
                ),
                errors = exception.errorFields?.map {
                    ErrorFieldDtoRs(
                        key = it.getKey(),
                        code = it.getCode(),
                        description = it.getDescription()
                    )
                }
            )
        )
    }

    // Обработка 5** ошибок

    @ExceptionHandler(CriticalException::class)
    fun handleCriticalException(
        exception: CriticalException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity.status(exception.httpStatus).body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = exception.errorCode.getCode(),
                    description = exception.errorCode.getDescription()
                )
            )
        )
    }

    @ExceptionHandler(Throwable::class)
    fun handleCriticalException(
        exception: Throwable,
        request: HttpServletRequest
    ): ResponseEntity<ErrorDtoRs> {

        processError(exception, request)

        return ResponseEntity.internalServerError().body(
            ErrorDtoRs(
                status = StatusDtoRs(
                    code = HttpStatus.INTERNAL_SERVER_ERROR.name,
                    description = "Внутренняя ошибка сервиса"
                )
            )
        )
    }

    private fun processError(throwable: Throwable, request: HttpServletRequest) {
        logError(throwable) { getRequestError(request) }
        observationRegistry.currentObservation?.error(throwable)
    }

    companion object : Loggable {

        private const val BAD_REQUEST_MESSAGE = "Некорректный формат запроса"
        private const val UNPROCESSABLE_ENTITY_REQUEST_MESSAGE = "Ошибка валидации"
        private const val FIELD_BAD_VALUE_CODE = "BAD_VALUE"

        private val INVALID_FORMAT_FIELD_REGEXP = """[^"]*("[^"]+")[^"]*$""".toRegex()
        private val MISMATCHED_FIELD_REGEXP = "(?<=\\[\")(.*?)(?=\"])".toRegex()

        private fun getRequestError(request: HttpServletRequest): String {
            return "Ошибка обработки запроса [method = ${request.method} path = ${request.requestURI}]"
        }

        fun extractInvalidFieldPath(exception: JsonMappingException): String? {
            if (exception.path.isNotEmpty()) {
                val paths = exception.path.map {
                    if (it.fieldName.isNotNullOrEmpty()) {
                        it.fieldName!!
                    } else {
                        "[${it.index}]"
                    }
                }
                return paths.reduce { acc, s ->
                    if (s.startsWith("[")) {
                        acc + s
                    } else {
                        "$acc.$s"
                    }
                }
            }

            if (exception.message.isNullOrEmpty()) {
                return null
            }

            val invalidFieldPath = extractInvalidFieldPath(exception.message)
            if (invalidFieldPath.isNotNullOrEmpty()) {
                return invalidFieldPath
            }

            val matchResult = INVALID_FORMAT_FIELD_REGEXP.find(exception.message!!)
            return matchResult?.groupValues?.get(1)?.trim('"')
        }

        fun extractInvalidFieldPath(exceptionMessage: String?): String? {
            if (exceptionMessage == null) return null
            val mismatchedFieldKey = MISMATCHED_FIELD_REGEXP.findAll(exceptionMessage)
                .map { it.value }
                .joinToString(separator = ".")

            return mismatchedFieldKey.nullIfEmpty()
        }

        fun extractFieldFromWebExchangeBindException(input: String): String {
            val index = input.lastIndexOf(WEB_EXCHANGE_BIND_EXCEPTION_DELIMITER)

            return input.substring(index + WEB_EXCHANGE_BIND_EXCEPTION_DELIMITER.length).trim()
        }
    }
}
