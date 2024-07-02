package ru.nemodev.platform.core.api.dto.error

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Ответ с ошибкой")
open class ErrorDtoRs(
    @Schema(description = "Статус")
    val status: StatusDtoRs,

    @Schema(description = "Список полей с ошибками")
    val errors: List<ErrorFieldDtoRs>? = null
)

@Schema(description = "Описание ошибочного поля")
data class ErrorFieldDtoRs(
    @Schema(description = "Ключ ошибочного поля", example = "sum", minLength = 3, maxLength = 255)
    val key: String,

    @Schema(description = "Код ошибки", example = "MIN_SUM", minLength = 3, maxLength = 255)
    val code: String,

    @Schema(description = "Описание ошибки", example = "Минимальная сумма должна быть больше 1 рубля", maxLength = 255)
    val description: String
)

@Schema(description = "Статус")
data class StatusDtoRs(
    @Schema(description = "Код статуса", example = "VALIDATION_ERROR", minLength = 3, maxLength = 255)
    val code: String,

    @Schema(description = "Описание кода статуса", minLength = 0, example = "Ошибка валидации запроса", maxLength = 255)
    val description: String
)