package ru.nemodev.platform.core.buildinfo.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.ClassPathResource
import ru.nemodev.platform.core.logging.sl4j.Loggable

class ApplicationInfoAppRunListener(
    private val springApplication: SpringApplication,
    private val args: Array<String>
) : SpringApplicationRunListener {

    companion object : Loggable {
        const val APPLICATION_FILE_NAME = "application.yml"
    }

    override fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext,
        environment: ConfigurableEnvironment
    ) {
        super.environmentPrepared(bootstrapContext, environment)

        try {
            val applicationProperties = YamlPropertiesFactoryBean().apply {
                setResources(ClassPathResource(APPLICATION_FILE_NAME))
            }.`object`!!

            val platformSdkVersion = ApplicationInfoAppRunListener::class.java.`package`.implementationVersion ?: "unknown"
            val applicationPropertySources = MapPropertySource(
                APPLICATION_FILE_NAME,
                applicationProperties.keys.associate { it.toString() to applicationProperties.getProperty(it.toString()) }
                + mapOf("platform.core.build-info.platform-sdk-version" to platformSdkVersion)
            )

            environment.propertySources.addLast(applicationPropertySources)
        } catch (e: Exception) {
            logError(e) {
                "Ошибка загрузки файла $APPLICATION_FILE_NAME для build-info"
            }
        }
    }
}