package ru.nemodev.platform.core.integration.s3.minio.client

import io.micrometer.observation.ObservationRegistry
import io.minio.ObjectWriteResponse
import io.minio.StatObjectResponse
import org.springframework.util.unit.DataSize
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties
import ru.nemodev.platform.core.integration.s3.minio.tracing.MinioContext
import ru.nemodev.platform.core.integration.s3.minio.tracing.MinioObservationConvention
import ru.nemodev.platform.core.tracing.extensions.createNotStarted
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.time.Duration
import java.util.function.Supplier

class MinioS3ClientObservation(
    private val delegate: MinioS3Client,
    private val properties: S3MinioProperties,
    private val observationRegistry: ObservationRegistry,
) : MinioS3Client by delegate {

    companion object {
        const val S3_FILE_NAME_KEY = "aws.s3.file.name"
        const val S3_FILE_LINK_LIVE_TIME_KEY = "aws.s3.file.link.livetime"
        const val S3_FILE_CONTENT_TYPE_KEY = "aws.s3.file.content.type"
        const val S3_FILE_SIZE_KEY = "aws.s3.file.size"
    }

    override fun upload(
        bucket: String?,
        fileName: String,
        fileContentType: String?,
        file: InputStream,
        fileSize: DataSize,
        filePartSize: DataSize?
    ): ObjectWriteResponse {
        return observationRegistry
            .createNotStarted(
                MinioObservationConvention.instance,
                MinioContext(
                    name = "file upload",
                    remoteServiceAddress = properties.httpClient.url,
                    bucket = bucket ?: properties.bucket
                )
            )
            .highCardinalityKeyValue(S3_FILE_NAME_KEY, fileName)
            .lowCardinalityKeyValue(S3_FILE_CONTENT_TYPE_KEY, fileContentType ?: properties.fileContentType)
            .highCardinalityKeyValue(S3_FILE_SIZE_KEY, fileSize.toString())
            .observe(Supplier {
                delegate.upload(bucket, fileName, fileContentType, file, fileSize, filePartSize)
            })!!
    }

    override fun upload(
        bucket: String?,
        fileName: String,
        fileContentType: String?,
        file: ByteArray,
        filePartSize: DataSize?
    ): ObjectWriteResponse {
        return upload(
            bucket = bucket,
            fileName = fileName,
            fileContentType = fileContentType,
            file = ByteArrayInputStream(file),
            fileSize = DataSize.ofBytes(file.size.toLong()),
            filePartSize = filePartSize
        )
    }

    override fun generateLink(bucket: String?, fileName: String, liveTime: Duration): URL {
        return observationRegistry
            .createNotStarted(
                MinioObservationConvention.instance,
                MinioContext(
                    name = "file generate link",
                    remoteServiceAddress = properties.httpClient.url,
                    bucket = bucket ?: properties.bucket
                )
            )
            .highCardinalityKeyValue(S3_FILE_NAME_KEY, fileName)
            .highCardinalityKeyValue(S3_FILE_LINK_LIVE_TIME_KEY, liveTime.toSeconds().toString())
            .observe(Supplier {
                delegate.generateLink(bucket, fileName, liveTime)
            })!!
    }

    override fun download(bucket: String?, fileName: String): InputStream {
        return observationRegistry
            .createNotStarted(
                MinioObservationConvention.instance,
                MinioContext(
                    name = "file download",
                    remoteServiceAddress = properties.httpClient.url,
                    bucket = bucket ?: properties.bucket
                )
            )
            .highCardinalityKeyValue(S3_FILE_NAME_KEY, fileName)
            .observe(Supplier {
                delegate.download(bucket, fileName)
            })!!
    }

    override fun fileParams(bucket: String?, fileName: String): StatObjectResponse {
        return observationRegistry
            .createNotStarted(
                MinioObservationConvention.instance,
                MinioContext(
                    name = "file params",
                    remoteServiceAddress = properties.httpClient.url,
                    bucket = bucket ?: properties.bucket
                )
            )
            .highCardinalityKeyValue(S3_FILE_NAME_KEY, fileName)
            .observe(Supplier {
                delegate.fileParams(bucket, fileName)
            })!!
    }

    override fun remove(bucket: String?, fileName: String) {
        return observationRegistry
            .createNotStarted(
                MinioObservationConvention.instance,
                MinioContext(
                    name = "file remove",
                    remoteServiceAddress = properties.httpClient.url,
                    bucket = bucket ?: properties.bucket
                )
            )
            .highCardinalityKeyValue(S3_FILE_NAME_KEY, fileName)
            .observe {
                delegate.remove(bucket, fileName)
            }
    }
}