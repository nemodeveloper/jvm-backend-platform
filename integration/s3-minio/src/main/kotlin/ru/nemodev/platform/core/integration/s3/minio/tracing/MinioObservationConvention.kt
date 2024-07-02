package ru.nemodev.platform.core.integration.s3.minio.tracing

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationConvention
import io.micrometer.observation.transport.Kind
import io.micrometer.observation.transport.Propagator
import io.micrometer.observation.transport.SenderContext
import io.opentelemetry.semconv.SemanticAttributes

class MinioContext(
    name: String,
    remoteServiceAddress: String,
    bucket: String
): SenderContext<Any>(
    Propagator.Setter { _: Any?, _: String?, _: String? -> },
    Kind.CLIENT
) {
    companion object {
        const val REMOTE_SERVICE_NAME_VALUE = "S3 Minio"
    }

    init {
        this.name = name
        this.remoteServiceName = REMOTE_SERVICE_NAME_VALUE
        this.remoteServiceAddress = remoteServiceAddress
        this.addLowCardinalityKeyValue(
            KeyValue.of(
                SemanticAttributes.AWS_S3_BUCKET.key, bucket
            )
        )
    }
}

class MinioObservationConvention : ObservationConvention<MinioContext> {

    companion object {
        val instance = MinioObservationConvention()
    }

    override fun supportsContext(context: Observation.Context): Boolean {
        return context is MinioContext
    }
}