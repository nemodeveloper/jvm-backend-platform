package ru.nemodev.platform.core.validation.config

import org.springframework.beans.factory.ListableBeanFactory
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.validation.Validator
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import ru.nemodev.platform.core.validation.validator.CompositeValidator
import ru.nemodev.platform.core.validation.validator.date.DateTimeRangeValidator

@AutoConfiguration
class ValidationConfig(
    private val listableBeanFactory: ListableBeanFactory
): WebMvcConfigurer {

    @Override
    override fun getValidator() = CompositeValidator(
        listableBeanFactory.getBeansOfType(Validator::class.java).values.toList()
    )

    @Bean
    fun dateTimeRangeValidator() = DateTimeRangeValidator()
}