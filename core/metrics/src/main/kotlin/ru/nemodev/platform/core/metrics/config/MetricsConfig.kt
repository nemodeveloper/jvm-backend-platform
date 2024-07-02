package ru.nemodev.platform.core.metrics.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@PropertySource(value = ["classpath:core-metrics.yml"], factory = YamlPropertySourceFactory::class)
class MetricsConfig