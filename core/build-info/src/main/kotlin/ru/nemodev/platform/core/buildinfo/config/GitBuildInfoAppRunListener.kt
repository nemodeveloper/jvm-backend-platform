package ru.nemodev.platform.core.buildinfo.config

import org.springframework.boot.ConfigurableBootstrapContext
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.io.ClassPathResource
import ru.nemodev.platform.core.logging.sl4j.Loggable
import java.util.*

class GitBuildInfoAppRunListener(
    private val springApplication: SpringApplication,
    private val args: Array<String>
) : SpringApplicationRunListener {

    companion object : Loggable {
        const val GIT_PROPERTY_FILE_NAME = "git.properties"
        const val GIT_PROPERTY_NAME = "git"
    }

    override fun environmentPrepared(
        bootstrapContext: ConfigurableBootstrapContext,
        environment: ConfigurableEnvironment
    ) {
        super.environmentPrepared(bootstrapContext, environment)

        try {
            val gitProperties = Properties().apply {
                load(ClassPathResource(GIT_PROPERTY_FILE_NAME).inputStream)
            }

            val gitPropertySources = MapPropertySource(
                GIT_PROPERTY_NAME,
                gitProperties.keys.associate { it.toString() to gitProperties.getProperty(it.toString()) }
            )

            environment.propertySources.addLast(gitPropertySources)
        } catch (e: Exception) {
            logError(e) {
                "Ошибка загрузки файла $GIT_PROPERTY_FILE_NAME для build-info"
            }
        }
    }
}