package ru.nemodev.platform.core.validation.validator.date

import org.springframework.validation.Errors
import org.springframework.validation.Validator
import java.time.LocalDateTime

class DateTimeRangeValidator : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return DateTimeRangeCriteria::class.java.isAssignableFrom(clazz)
    }

    override fun validate(target: Any, errors: Errors) {
        target as DateTimeRangeCriteria
        if (target.getFromDateTime().isAfter(target.getToDateTime())) {
            errors.rejectValue(
                target.getFromDateTimeField(),
                target.getValidationErrorCode(),
                target.getFromDateTimeMessage()
            )
            errors.rejectValue(
                target.getToDateTimeField(),
                target.getValidationErrorCode(),
                target.getToDateTimeMessage()
            )
        }
    }
}

interface DateTimeRangeCriteria {
    fun getFromDateTime(): LocalDateTime
    fun getFromDateTimeField(): String
    fun getFromDateTimeMessage(): String = "Дата начала должна быть до даты окончания"

    fun getToDateTime(): LocalDateTime
    fun getToDateTimeField(): String
    fun getToDateTimeMessage(): String = "Дата окончания должна быть после даты начала"

    fun getValidationErrorCode(): String = DATE_TIME_RANGE_NOT_VALID_ERROR_CODE

    companion object {
        const val DATE_TIME_RANGE_NOT_VALID_ERROR_CODE = "DATE_TIME_RANGE_NOT_VALID"
    }
}

data class CreatedAtDateTimeRangeCriteria(
    val fromCreatedAt: LocalDateTime,
    val toCreatedAt: LocalDateTime
) : DateTimeRangeCriteria {

    companion object {
        const val FROM_CREATED_AT_FIELD = "fromCreatedAt"
        const val TO_CREATED_AT_FIELD = "toCreatedAt"
    }

    override fun getFromDateTime() = fromCreatedAt
    override fun getFromDateTimeField() = FROM_CREATED_AT_FIELD

    override fun getToDateTime() = toCreatedAt
    override fun getToDateTimeField() = TO_CREATED_AT_FIELD
}