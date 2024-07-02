package ru.nemodev.platform.core.integration.s3.minio.client

import io.minio.*
import io.minio.http.Method
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.springframework.util.unit.DataSize
import ru.nemodev.platform.core.integration.s3.minio.config.S3MinioProperties
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.URL
import java.time.Duration
import java.util.concurrent.TimeUnit

interface MinioS3Client {

    val minioClient: MinioClient

    fun upload(
        bucket: String? = null,
        fileName: String,
        fileContentType: String? = null,
        file: InputStream,
        fileSize: DataSize,
        filePartSize: DataSize? = null
    ): ObjectWriteResponse

    fun upload(
        bucket: String? = null,
        fileName: String,
        fileContentType: String? = null,
        file: ByteArray,
        filePartSize: DataSize? = null
    ): ObjectWriteResponse

    fun generateLink(
        bucket: String? = null,
        fileName: String,
        liveTime: Duration
    ): URL

    fun download(
        bucket: String? = null,
        fileName: String
    ): InputStream

    fun fileParams(
        bucket: String? = null,
        fileName: String
    ): StatObjectResponse

    fun remove(
        bucket: String? = null,
        fileName: String
    )
}

class MinioS3ClientImpl(
    private val properties: S3MinioProperties,
    override val minioClient: MinioClient
) : MinioS3Client {

    override fun upload(
        bucket: String?,
        fileName: String,
        fileContentType: String?,
        file: InputStream,
        fileSize: DataSize,
        filePartSize: DataSize?
    ): ObjectWriteResponse {
        return minioClient.putObject(
            PutObjectArgs.Builder()
                .bucket(bucket ?: properties.bucket)
                .`object`(fileName)
                .contentType(fileContentType ?: properties.fileContentType)
                .stream(
                    file,
                    fileSize.toBytes(),
                    filePartSize?.toBytes() ?: properties.uploadFilePartSize.toBytes()
                )
                .build()
        )
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

    override fun generateLink(
        bucket: String?,
        fileName: String,
        liveTime: Duration
    ): URL {
        return minioClient.getPresignedObjectUrl(
            GetPresignedObjectUrlArgs.builder()
                .bucket(bucket ?: properties.bucket)
                .`object`(fileName)
                .method(Method.GET)
                .expiry(liveTime.toSeconds().toInt(), TimeUnit.SECONDS)
                .build()
        ).toHttpUrl().toUrl()
    }

    override fun download(
        bucket: String?,
        fileName: String
    ): InputStream {
       return minioClient.getObject(
           GetObjectArgs.builder()
               .bucket(bucket ?: properties.bucket)
               .`object`(fileName)
               .build()
       )
    }

    override fun fileParams(
        bucket: String?,
        fileName: String
    ): StatObjectResponse {
        return minioClient.statObject(
            StatObjectArgs.builder()
                .bucket(bucket ?: properties.bucket)
                .`object`(fileName)
                .build()
        )
    }

    override fun remove(bucket: String?, fileName: String) {
        minioClient.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket ?: properties.bucket)
                .`object`(fileName)
                .build()
        )
    }
}