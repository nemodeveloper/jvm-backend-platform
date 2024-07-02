package ru.nemodev.platform.core.validation.validator.annotation

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [PatternListValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class PatternList(
    val message: String,
    val regexp: String,
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Any>> = []
)

// ИЗМЕНЕНИЕ ДАННОГО КЛАССА ЗАТРАГИВАЕТ fun handleBadRequestValidationExceptions в классе ApiExceptionHandler !!!
// А ТАКЖЕ ValidatorConst !!!
class PatternListValidator : ConstraintValidator<PatternList, List<String>> {
    private lateinit var regexp: Regex
    private lateinit var message: String

    override fun initialize(constraintAnnotation: PatternList) {
        regexp = constraintAnnotation.regexp.toRegex()
        message = constraintAnnotation.message
    }

    override fun isValid(value: List<String>?, context: ConstraintValidatorContext): Boolean {
        if (value == null) {
            return true
        }

        var isValid = true

        value.forEachIndexed { index, item ->
            if (!regexp.matches(item)) {
                isValid = false
                context.disableDefaultConstraintViolation()

                // Изменение данного кода ЗАТРАГИВАЕТ переменную fieldName
                // в fun handleBadRequestValidationExceptions в классе ApiExceptionHandler
                // а также ValidatorConst !!!
                context.buildConstraintViolationWithTemplate("[$index]$message")
                    .addConstraintViolation()
            }
        }

        return isValid
    }
}