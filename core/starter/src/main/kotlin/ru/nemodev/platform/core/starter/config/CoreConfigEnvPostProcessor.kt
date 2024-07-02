package ru.nemodev.platform.core.starter.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.EncodedResource
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

class CoreConfigEnvPostProcessor : EnvironmentPostProcessor {

    companion object {
        const val FILE_NAME = "core-starter.yml"
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        environment?.propertySources?.addLast(
            YamlPropertySourceFactory()
                .createPropertySource(
                    name = FILE_NAME,
                    encodedResource = EncodedResource(
                        ClassPathResource(FILE_NAME),
                    )
                )
        )
    }
}