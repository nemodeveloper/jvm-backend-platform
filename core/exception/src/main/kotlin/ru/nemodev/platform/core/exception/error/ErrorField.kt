package ru.nemodev.platform.core.exception.error

interface ErrorField {
    fun getKey(): String
    fun getCode(): String
    fun getDescription(): String

    companion object {
        fun create(key: String, code: String, description: String): ErrorField
            = ErrorFieldImpl(key = key, code = code, description = description)
    }
}

data class ErrorFieldImpl(
    private val key: String,
    private val code: String,
    private val description: String
): ErrorField {
    override fun getKey() = key
    override fun getCode() = code
    override fun getDescription() = description
    override fun toString() = "Ошибочное поле: ключ = $key, код = $code, описание = $description"
}