package ru.nemodev.platform.core.integration.s3.minio.config

import io.micrometer.observation.ObservationRegistry
import io.minio.MinioClient
import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.PropertySource
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3Client
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3ClientImpl
import ru.nemodev.platform.core.integration.s3.minio.client.MinioS3ClientObservation
import ru.nemodev.platform.core.spring.config.YamlPropertySourceFactory

@AutoConfiguration
@EnableConfigurationProperties(S3MinioProperties::class)
@PropertySource(value = ["classpath:core-s3-minio.yml"], factory = YamlPropertySourceFactory::class)
class S3MinioConfig {

    @Bean
    fun minioS3Client(
        observationRegistry: ObservationRegistry,
        properties: S3MinioProperties
    ): MinioS3Client {
        val client = MinioS3ClientImpl(
            properties,
            MinioClient.builder()
                .endpoint(properties.httpClient.url)
                .credentials(properties.accessKey, properties.secretKey)
                .httpClient(
                    OkHttpClient()
                        .newBuilder()
                        .connectTimeout(properties.httpClient.timeout.connection)
                        .readTimeout(properties.httpClient.timeout.read)
                        .build()
                )
                .build()
        )

        return if (properties.httpClient.observationEnabled) {
            MinioS3ClientObservation(
                client,
                properties,
                observationRegistry
            )
        } else {
            client
        }
    }
}