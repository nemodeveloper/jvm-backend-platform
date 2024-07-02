package ru.nemodev.platform.core.db.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("platform.core.db")
data class DataBaseProperties(
    @DefaultValue("ru.nemodev")
    val storeJsonBasePackage: String,
    @DefaultValue("true")
    val throwExceptionIfIdNull: Boolean
)
