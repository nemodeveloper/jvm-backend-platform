package ru.nemodev.platform.core.db.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.postgresql.util.PGobject
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.context.annotation.Primary
import org.springframework.core.convert.TypeDescriptor
import org.springframework.core.convert.converter.GenericConverter
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions
import ru.nemodev.platform.core.db.annotation.StoreJson

@AutoConfiguration
class StoreJsonConfig {

    @Bean
    @Primary
    fun storeJsonCustomConversions(
        objectMapper: ObjectMapper,
        coreDbProperties: DataBaseProperties
    ): JdbcCustomConversions = JdbcCustomConversions(
        ClassPathScanningCandidateComponentProvider(false)
            .apply {
                addIncludeFilter(AnnotationTypeFilter(StoreJson::class.java))
            }
            .findCandidateComponents(coreDbProperties.storeJsonBasePackage)
            .asSequence()
            .map { Class.forName(it.beanClassName) }
            .map { clz ->
                listOf(
                    @WritingConverter
                    object : GenericConverter {
                        override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(clz, PGobject::class.java))
                        override fun convert(source: Any?, p1: TypeDescriptor, p2: TypeDescriptor) =
                            PGobject().apply {
                                type = "jsonb"
                                value = objectMapper.writeValueAsString(source)
                            }
                    },
                    @ReadingConverter
                    object : GenericConverter {
                        override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(PGobject::class.java, clz))
                        override fun convert(source: Any?, p1: TypeDescriptor, p2: TypeDescriptor) =
                            objectMapper.readValue((source as PGobject).value, clz)
                    }
                )
            }
            .flatten()
            .plus(
                listOf(
                    @WritingConverter
                    object : GenericConverter {
                        override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(PGobject::class.java, PGobject::class.java))
                        override fun convert(source: Any?, p1: TypeDescriptor, p2: TypeDescriptor) = source
                    },
                    @ReadingConverter
                    object : GenericConverter {
                        override fun getConvertibleTypes() = setOf(GenericConverter.ConvertiblePair(PGobject::class.java, PGobject::class.java))
                        override fun convert(source: Any?, p1: TypeDescriptor, p2: TypeDescriptor) = source
                    }
                )
            )
            .toList()
    )
}
