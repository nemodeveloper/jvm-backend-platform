package ru.nemodev.platform.core.integration.kafka.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@PropertySource(value = ["classpath:core-async-api.yml"], factory = YamlPropertySourceFactory::class)
class AsyncApiConfig
