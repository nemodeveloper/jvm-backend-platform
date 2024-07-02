package ru.nemodev.platform.core.openapi

import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.ComposedSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.servers.Server
import org.springdoc.core.models.GroupedOpenApi
import ru.nemodev.platform.core.openapi.config.OpenApiProperties

fun defineOpenApi(
    group: String,
    title: String,
    contact: OpenApiProperties.Contact,
    prefix: String = "",
    packageName: String? = null,
    version: String = "1",
    urlPrefix: String = "$prefix/v$version",
    description: String? = null,
    servers: List<OpenApiProperties.Server>,
    basePath: String?  = ""
): GroupedOpenApi = GroupedOpenApi.builder()
        .group(group)
        .pathsToMatch(prefix, "$prefix/**")
        .apply {
            if (packageName != null) packagesToScan(packageName)
        }
        .addOpenApiCustomizer { openApi ->

            val suffix = basePath + urlPrefix
            val swaggerServers = servers.map { serverProperty ->
                Server().apply {
                    url = serverProperty.url + suffix
                    this.description = serverProperty.name
               }
            }

            openApi.info(
                Info().title(title)
                    .version(version)
                    .description(description)
                    .contact(io.swagger.v3.oas.models.info.Contact().apply {
                        name = contact.name
                        email = contact.email
                        url = contact.url
                    })
            ).servers(swaggerServers)
                .paths.apply {// Убираем версионные префиксы у путей
                    keys.toSet()
                        .filter { it.startsWith(urlPrefix) }
                        .forEach {
                            put(it.removePrefix(urlPrefix), get(it))
                            remove(it)
                        }
                }

            openApi.components.schemas.values.forEach { s ->
                upgradeSchema(s)
            }
        }
        .build()

private fun upgradeSchema(s: Schema<*>) {
    if (s.type == "object") {
        s.additionalProperties = false
    }
    if (s.type == "string" && s.maxLength == null && s.pattern == null) {
        s.maxLength = 255
    }
    if (s.type == "array" && s.maxItems == null) {
        s.maxItems = 1000
    }
    s.properties?.forEach { it ->
        upgradeSchema(it.value)
    }

    if (s is ComposedSchema) {
        s.allOf?.map { upgradeSchema(it) }
        s.oneOf?.map { upgradeSchema(it) }
        s.anyOf?.map { upgradeSchema(it) }
    }
}
