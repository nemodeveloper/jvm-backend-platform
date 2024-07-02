package ru.nemodev.platform.core.logging.constant

enum class LoggingFormat {
    /**
     * Стандартный формат для вывода в консоль
     */
    PLAIN,

    /**
     * Форматированный JSON в формате pretty, используйте для локальной отладки
     */
    JSON_PRETTY,

    /**
     * Однострочный JSON, используйте на STAGE/PROD
     */
    JSON_COMPACT,
    ;

    fun isPlain() = this == PLAIN
    fun isJsonPretty() = this == JSON_PRETTY
    fun isJsonCompact() = this == JSON_COMPACT
}
