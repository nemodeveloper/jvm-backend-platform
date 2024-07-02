package ru.nemodev.platform.core.openapi.processor

import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.env.Environment
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nemodev.platform.core.extensions.isNumeric
import ru.nemodev.platform.core.openapi.config.OpenApiProperties
import ru.nemodev.platform.core.openapi.defineOpenApi

class OpenApiBeanDefinitionRegistryPostProcessor(
    environment: Environment,
) : BeanDefinitionRegistryPostProcessor {

    companion object {
        // TODO: вынести базовый пакет в пропертю с дефолтным значением
        private const val BASE_PACKAGE = "ru.nemodev"
        private const val BASE_PATH = "server.servlet.context-path"
        private const val PROPERTIES_PREFIX = "platform.core.open-api"
        private const val APPLICATION_NAME = "spring.application.name"

        private const val TAG_ANNOTATION_DESCRIPTION_FIELD = "description"
        private const val REQUEST_MAPPING_ANNOTATION_VALUE_FIELD = "value"
    }

    private val properties: OpenApiProperties
    private val basePath: String
    private val applicationName: String

    init {
        val binder = Binder.get(environment)
        basePath = binder.bind(BASE_PATH, String::class.java).orElse("")
        properties = try {
            binder.bind(PROPERTIES_PREFIX, OpenApiProperties::class.java).get()
        } catch (e: NoSuchElementException) {
            OpenApiProperties(true)
        }
        applicationName = binder.bind(APPLICATION_NAME, String::class.java).get()
    }

    override fun postProcessBeanDefinitionRegistry(
        registry: BeanDefinitionRegistry
    ) {
        // TODO: придумать решение лучше, чем создание и удаление бина
        registry.removeBeanDefinition("removeMe")

        ClassPathScanningCandidateComponentProvider(false)
            .apply { addIncludeFilter(AnnotationTypeFilter(RestController::class.java)) }
            .findCandidateComponents(BASE_PACKAGE)
            .forEachIndexed { i, controller ->
                if (controller is AnnotatedBeanDefinition) {
                    val tagAttributes = controller.metadata.getAnnotationAttributes(Tag::class.qualifiedName!!)!!
                    val requestMappingAttributes = controller.metadata
                        .getAnnotationAttributes(RequestMapping::class.qualifiedName!!)!!

                    val version: String =
                        ((requestMappingAttributes[REQUEST_MAPPING_ANNOTATION_VALUE_FIELD] as Array<*>)[0] as String)
                            .substringAfter("/v")
                            .substringBefore("/")
                            .takeIf { substring -> substring.isNumeric() }
                            ?: "1"

                    val openApi = defineOpenApi(
                        group = when {
                            controller.isAdminController() -> "$applicationName-admin-v$version"
                            controller.isAuthController() -> "$applicationName-auth-v$version"
                            else -> "$applicationName-v$version"
                        },
                        title = tagAttributes[TAG_ANNOTATION_DESCRIPTION_FIELD] as String,
                        contact = properties.contact,
                        prefix = if (controller.isAdminController()) "/admin" else "",
                        packageName = controller.beanClassName!!.substringBeforeLast("."),
                        version = version,
                        basePath = basePath,
                        servers = properties.servers
                    )

                    val bean = BeanDefinitionBuilder
                        .genericBeanDefinition(GroupedOpenApi::class.java) {
                            openApi
                        }
                        .beanDefinition
                        .also { bean -> bean.scope = BeanDefinition.SCOPE_PROTOTYPE }

                    registry.registerBeanDefinition(
                        "${openApi::class.java.canonicalName}_$i}",
                        bean
                    )
                }
            }
    }

    private fun AnnotatedBeanDefinition.isAdminController(): Boolean {
        val isAdminPackage = beanClassName
            ?.contains("api.admin", ignoreCase = true)
            ?: false
        val containsAdminInClassName = beanClassName
            ?.substringAfterLast(".")
            ?.contains("admin", ignoreCase = true)
            ?: false
        return isAdminPackage || containsAdminInClassName
    }

    private fun AnnotatedBeanDefinition.isAuthController(): Boolean {
        val isAdminPackage = beanClassName
            ?.contains("api.auth", ignoreCase = true)
            ?: false
        val containsAdminInClassName = beanClassName
            ?.substringAfterLast(".")
            ?.contains("auth", ignoreCase = true)
            ?: false
        return isAdminPackage || containsAdminInClassName
    }
}
