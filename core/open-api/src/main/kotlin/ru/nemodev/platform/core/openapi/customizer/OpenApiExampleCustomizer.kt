package ru.nemodev.platform.core.openapi.customizer

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import org.springdoc.core.customizers.GlobalOpenApiCustomizer
import java.io.FileNotFoundException

class OpenApiExampleCustomizer(
    private val objectMapper: ObjectMapper
) : GlobalOpenApiCustomizer {

    override fun customise(openApi: OpenAPI) {
        openApi.paths.entries.forEach { path -> createExampleTextForRequestBody(path.value.get) }
        openApi.paths.entries.forEach { path -> createExampleTextForRequestBody(path.value.put) }
        openApi.paths.entries.forEach { path -> createExampleTextForRequestBody(path.value.post) }
        openApi.paths.entries.forEach { path -> createExampleTextForRequestBody(path.value.patch) }
        openApi.paths.entries.forEach { path -> createExampleTextForRequestBody(path.value.delete) }

        openApi.paths.entries.forEach { path -> createExampleTextForResponse(path.value.get) }
        openApi.paths.entries.forEach { path -> createExampleTextForResponse(path.value.put) }
        openApi.paths.entries.forEach { path -> createExampleTextForResponse(path.value.post) }
        openApi.paths.entries.forEach { path -> createExampleTextForResponse(path.value.patch) }
        openApi.paths.entries.forEach { path -> createExampleTextForResponse(path.value.delete) }
    }

    private fun createExampleTextForRequestBody(operation: Operation?) {
        operation?.requestBody?.content?.asSequence()
            ?.map { content -> content.value?.schema }
            ?.filter { schema -> schema?.example != null }
            ?.forEach { schema -> schema?.example = createExampleTextFromExampleString(schema?.example.toString()) }
    }

    private fun createExampleTextForResponse(operation: Operation?) {
        operation?.responses?.asSequence()
            ?.forEach { response -> response.value?.content?.asSequence()
                ?.map { content -> content.value }
                ?.filter { contentValue -> contentValue?.example != null }
                ?.forEach { contentValue ->
                    contentValue?.example = createExampleTextFromExampleString(contentValue?.example.toString())
                }
            }
    }

    private fun createExampleTextFromExampleString(exampleString: String): Any {
        if (exampleString.startsWith("@")) {
            val url = this::class.java.classLoader.getResource(exampleString.drop(1)) ?:
            throw FileNotFoundException("Не удалось найти файл для примера API = [$exampleString]")
            return objectMapper.readTree(url.readText())
        }
        return exampleString
    }
}
