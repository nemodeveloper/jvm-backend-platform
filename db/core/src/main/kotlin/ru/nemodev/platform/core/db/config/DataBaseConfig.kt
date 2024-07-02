package ru.nemodev.platform.core.db.config

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback
import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import ru.nemodev.platform.core.db.callback.UpdateAfterSaveCallback
import ru.nemodev.platform.core.db.callback.UpdateBeforeConvertCallback
import ru.nemodev.platform.core.service.generator.IdGeneratorService
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@EnableConfigurationProperties(DataBaseProperties::class)
@PropertySource(value = ["classpath:core-db.yml"], factory = YamlPropertySourceFactory::class)
class DataBaseConfig {

    @Bean
    fun updateBeforeConvertCallback(
        coreDbProperties: DataBaseProperties,
        idGeneratorService: IdGeneratorService
    ): BeforeConvertCallback<Any> = UpdateBeforeConvertCallback(coreDbProperties, idGeneratorService)

    @Bean
    fun updateAfterSaveCallback(): AfterSaveCallback<Any> = UpdateAfterSaveCallback()
}
