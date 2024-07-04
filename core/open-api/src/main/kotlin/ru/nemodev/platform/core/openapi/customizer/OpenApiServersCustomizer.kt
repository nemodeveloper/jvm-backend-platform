package ru.nemodev.platform.core.openapi.customizer

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import org.springdoc.core.properties.SpringDocConfigProperties

class OpenApiServersCustomizer(
    private val properties: SpringDocConfigProperties
) : GlobalOpenApiCustomizer {

    override fun customise(openApi: OpenAPI) {
        if (properties.openApi.servers.isNotEmpty()) {
            openApi.servers = properties.openApi.servers
        }
    }
}