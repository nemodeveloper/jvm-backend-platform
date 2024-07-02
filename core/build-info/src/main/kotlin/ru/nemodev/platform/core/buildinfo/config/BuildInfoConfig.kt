package ru.nemodev.platform.core.buildinfo.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.buildinfo.service.BuildInfoService
import ru.nemodev.platform.core.buildinfo.service.BuildInfoServiceImpl
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@PropertySource(value = ["classpath:core-build-info.yml"], factory = YamlPropertySourceFactory::class)
class BuildInfoConfig {

    @Bean
    fun buildInfoService(): BuildInfoService = BuildInfoServiceImpl()
}