package ru.nemodev.platform.core.spring.config

import org.springframework.beans.factory.config.YamlPropertiesFactoryBean
import org.springframework.core.env.PropertiesPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory

class YamlPropertySourceFactory : PropertySourceFactory {
    override fun createPropertySource(name: String?, encodedResource: EncodedResource): PropertySource<*> {
        return PropertiesPropertySource(
            encodedResource.resource.filename.orEmpty(),
            YamlPropertiesFactoryBean().apply {
                setResources(encodedResource.resource)
            }.`object`!!
        )
    }
}