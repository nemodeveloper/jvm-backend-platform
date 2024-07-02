package ru.nemodev.platform.core.openapi.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue

@ConfigurationProperties("platform.core.open-api")
data class OpenApiProperties(
    @DefaultValue("true")
    val enabled: Boolean,
    @DefaultValue
    @NestedConfigurationProperty
    val contact: Contact = Contact(
        name = "Симанов А.Н",
        url = "tg://simanovan",
        email = "nemodev@yandex.ru",
    ),
    val servers: List<Server> = listOf(
        Server(
            name = "LOCAL",
            url = "http://localhost:8080"
        )
    )
) {
    data class Contact(
        @DefaultValue("Симанов А.Н")
        val name: String,
        @DefaultValue("tg://simanovan")
        val url: String,
        @DefaultValue("nemodev@yandex.ru")
        val email: String
    )

    data class Server(
        val name: String,
        val url: String
    )
}
