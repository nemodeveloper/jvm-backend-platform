package ru.nemodev.platform.core.logging.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.EncodedResource
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

class CoreLoggingConfigEnvPostProcessor : EnvironmentPostProcessor {

    companion object {
        const val CONFIG_FILE_NAME = "core-logging.yml"
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        environment?.propertySources?.addLast(
            YamlPropertySourceFactory()
                .createPropertySource(
                    name = CONFIG_FILE_NAME,
                    encodedResource = EncodedResource(
                        ClassPathResource(CONFIG_FILE_NAME),
                    )
                )
        )
    }
}