package ru.nemodev.platform.core.logging.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.unit.DataSize
import ru.nemodev.platform.core.logging.constant.LoggingFormat

@ConfigurationProperties("platform.core.logging")
data class LoggingProperties(
    @DefaultValue("true")
    val consoleEnabled: Boolean,
    @DefaultValue("128KB")
    val messageMaxSize: DataSize,
    @DefaultValue("PLAIN")
    val format: LoggingFormat,
    @DefaultValue
    @NestedConfigurationProperty
    val body: BodyLogging,
    @DefaultValue
    val masking: List<MaskingPattern>,
    @DefaultValue("ru.nemodev")
    val logPackages: Set<String>
) {
    data class BodyLogging(
        @DefaultValue("JSON_PRETTY")
        val format: LoggingFormat,
        @DefaultValue
        val requestExcludeBodyPatterns: Set<String>,
        @DefaultValue
        val responseExcludeBodyPatterns: Set<String>,
        @DefaultValue
        @NestedConfigurationProperty
        val masking: MaskingBody
    ) {
        data class MaskingBody(
            @DefaultValue
            @NestedConfigurationProperty
            val jsonPaths: List<JsonPathBodyMasking>
        ) {
            data class JsonPathBodyMasking(
                val path: String,
                val replacementRegex: Regex?
            )
        }
    }

    data class MaskingPattern(
        val regex: Regex,
        val valueRegex: Regex?,
        val propertyName: String?
    )
}
