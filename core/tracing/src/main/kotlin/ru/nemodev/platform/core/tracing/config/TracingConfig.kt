package ru.nemodev.platform.core.tracing.config

import io.micrometer.observation.ObservationFilter
import io.micrometer.observation.ObservationPredicate
import io.micrometer.tracing.otel.bridge.EventListener
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.sdk.resources.Resource
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.actuate.autoconfigure.tracing.ConditionalOnEnabledTracing
import org.springframework.boot.actuate.autoconfigure.tracing.OpenTelemetryAutoConfiguration
import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpProperties
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import org.springframework.http.server.reactive.observation.ServerRequestObservationContext
import ru.nemodev.platform.core.buildinfo.service.BuildInfoService
import ru.nemodev.platform.core.environment.service.EnvironmentService
import ru.nemodev.platform.core.service.generator.IdGeneratorService
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory
import ru.nemodev.platform.core.tracing.observation.PlatformHeaderEventListener
import ru.nemodev.platform.core.tracing.observation.PlatformHeaderObservationFilter
import ru.nemodev.platform.core.tracing.observation.PlatformHeaderTextMapPropagator

@AutoConfiguration(before = [OpenTelemetryAutoConfiguration::class])
@ConditionalOnEnabledTracing
@EnableConfigurationProperties(TracingProperties::class)
@PropertySource(value = ["classpath:core-tracing.yml"], factory = YamlPropertySourceFactory::class)
class TracingConfig {

    companion object {
        val baseApiObservationPaths = setOf(
            "actuator",
            "swagger",
            "open-api",
            "api-docs",
            "webjars",
            "springwolf",
            "favicon"
        )
    }

    @Suppress("INACCESSIBLE_TYPE")
    @Bean
    fun otlpGrpcSpanExporter(
        properties: OtlpProperties
    ): OtlpGrpcSpanExporter {
        return OtlpGrpcSpanExporter.builder()
            .setEndpoint(properties.endpoint)
            .setTimeout(properties.timeout)
            .setCompression(properties.compression.toString().lowercase())
            .apply {
                properties.headers.forEach {
                    addHeader(it.key, it.value)
                }
            }
            .build()
    }

    @Bean
    @ConditionalOnProperty("platform.core.tracing.spring-security-enabled", havingValue = "false", matchIfMissing = true)
    fun excludeSpringSecurityObservationPredicate(): ObservationPredicate {
        return ObservationPredicate { name: String?, _ ->
            if (name.isNullOrEmpty()) {
                true
            } else {
                !name.startsWith("spring.security")
            }
        }
    }

    @Bean
    @ConditionalOnProperty("platform.core.tracing.base-api-enabled", havingValue = "false", matchIfMissing = true)
    fun excludeBaseApiObservationPredicate(): ObservationPredicate {
        return ObservationPredicate { _, context ->
            if (context is ServerRequestObservationContext) {
                val apiPath = context.carrier.uri.path
                !baseApiObservationPaths.any { apiPath.contains(it) }
            } else {
                true
            }
        }
    }

    @Bean
    fun platformHeaderTextMapPropagator(
        @Value("\${spring.application.name}")
        applicationName: String,
        idGeneratorService: IdGeneratorService
    ): TextMapPropagator = PlatformHeaderTextMapPropagator(applicationName, idGeneratorService)

    @Bean
    fun platformObservationFilter(
        @Value("\${spring.application.name}")
        applicationName: String,
    ): ObservationFilter = PlatformHeaderObservationFilter(applicationName)

    @Bean
    fun platformHeaderEventListener(): EventListener = PlatformHeaderEventListener()

    @Bean
    fun otelResourceBeanPostProcessor(
        buildInfoService: BuildInfoService,
        environmentService: EnvironmentService
    ): BeanPostProcessor {
        return object : BeanPostProcessor {
            override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
                return if (bean is Resource) {
                    bean.merge(
                        Resource.builder()
                            .put("platform.sdk.version", buildInfoService.getPlatformSdkVersion())
                            .put("platform.cloud.platform", environmentService.getCloudPlatform().name)
                            .put("platform.service.host", environmentService.getHost().name)
                            .put("platform.service.ip", environmentService.getHost().ip)
                            .build()
                    )
                } else {
                    bean
                }
            }
        }
    }
}