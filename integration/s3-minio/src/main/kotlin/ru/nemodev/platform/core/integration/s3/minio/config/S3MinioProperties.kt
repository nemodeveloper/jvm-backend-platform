package ru.nemodev.platform.core.integration.s3.minio.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.boot.context.properties.bind.DefaultValue
import org.springframework.util.unit.DataSize
import java.time.Duration

@ConfigurationProperties("platform.integration.s3-minio")
data class S3MinioProperties(
    val accessKey: String,
    val secretKey: String,
    val bucket: String,
    val fileContentType: String = "unknown",
    @DefaultValue("5MB")
    val uploadFilePartSize: DataSize,
    @NestedConfigurationProperty
    val httpClient: HttpClient
) {

    data class HttpClient(
        val serviceId: String,
        val url: String,
        @DefaultValue
        @NestedConfigurationProperty
        val timeout: Timeout,
        @DefaultValue("true")
        val observationEnabled: Boolean
    )

    data class Timeout(
        @DefaultValue("3s")
        val connection: Duration,
        @DefaultValue("3s")
        val read: Duration,
        @DefaultValue("3s")
        val write: Duration
    )
}