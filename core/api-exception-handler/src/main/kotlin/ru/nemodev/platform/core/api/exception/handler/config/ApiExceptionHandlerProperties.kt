package ru.nemodev.platform.core.api.exception.handler.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue
import ru.nemodev.platform.core.api.exception.handler.const.ValidatorConst.PATTERN_LIST_FIELD_ERROR_CODE

@ConfigurationProperties("platform.core.api-exception-handler")
data class ApiExceptionHandlerProperties(
    @DefaultValue
    val errorFields: List<ErrorField>
) {
    data class ErrorField(
        val internalCode: String,
        val externalCode: String,
    )

    companion object {
        const val NOT_EMPTY_FIELD_ERROR_CODE = "NOT_EMPTY"
        const val PATTERN_FIELD_ERROR_CODE = "PATTERN"
        const val NOT_SUPPORT_FIELD_ERROR_CODE = "NOT_SUPPORT"

        val DEFAULT_FIELD_ERROR_CODE_MAP = mapOf(
            "NotEmpty" to NOT_EMPTY_FIELD_ERROR_CODE,
            "NotBlank" to NOT_EMPTY_FIELD_ERROR_CODE,
            "NullOrNotBlank" to NOT_EMPTY_FIELD_ERROR_CODE,
            PATTERN_LIST_FIELD_ERROR_CODE to PATTERN_FIELD_ERROR_CODE,
        )
    }

    private val errorFieldMap = errorFields.associateBy { it.internalCode }
    fun getExternalErrorFieldCode(internalCode: String)
        = errorFieldMap[internalCode]?.externalCode
            ?: DEFAULT_FIELD_ERROR_CODE_MAP[internalCode]
                ?: internalCode.uppercase()

}
