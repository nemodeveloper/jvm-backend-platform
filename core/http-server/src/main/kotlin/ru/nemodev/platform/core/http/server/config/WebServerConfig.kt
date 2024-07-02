package ru.nemodev.platform.core.http.server.config

import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@PropertySource(value = ["classpath:core-http-server.yml"], factory = YamlPropertySourceFactory::class)
class WebServerConfig