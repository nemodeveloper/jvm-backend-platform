package ru.nemodev.platform.core.validation.validator

import org.springframework.validation.Errors
import org.springframework.validation.Validator

class CompositeValidator(
    private val validators: List<Validator>
) : Validator {

    override fun supports(clazz: Class<*>): Boolean {
        return validators.any { it.supports(clazz) }
    }

    override fun validate(target: Any, errors: Errors) {
        validators.forEach {
            if (it.supports(target.javaClass)) {
                it.validate(target, errors)
            }
        }
    }
}