package ru.nemodev.platform.core.tracing.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation.Context
import io.micrometer.observation.ObservationFilter
import io.micrometer.observation.transport.Kind
import io.micrometer.observation.transport.ReceiverContext
import io.micrometer.observation.transport.SenderContext
import ru.nemodev.platform.core.api.headers.ApiHeaderNames

class PlatformHeaderObservationFilter(
    private val applicationName: String
) : ObservationFilter {

    companion object {
        val platformHeaderHighCardinalityKeys = mapOf(
            ApiHeaderNames.REQUEST_ID to "platform.request.id",
            ApiHeaderNames.USER_ID to "platform.user.id",
            ApiHeaderNames.LOG_MODE to "platform.log.mode"
        )
    }

    override fun map(p0: Context): Context {
        // Context заполняется в PlatformHeaderTextMapPropagator
        val platformHeaders = io.opentelemetry.context.Context.current()
            ?.get(PlatformObservationConst.platformHeadersContextKey)
            ?: return p0

        platformHeaderHighCardinalityKeys.forEach { header ->
            platformHeaders[header.key]?.let {
                p0.addHighCardinalityKeyValue(
                    KeyValue.of(
                        header.value,
                        it
                    )
                )
            }
        }

        p0.addHighCardinalityKeyValue(
            KeyValue.of(
                "platform.service.initiator",
                getInitiatorService(p0, platformHeaders)
            )
        )

        return p0
    }

    private fun getInitiatorService(context: Context, platformHeaders: Map<String, String?>): String {
        return when (context) {
            is SenderContext<*> -> {
                when (context.kind) {
                    Kind.CLIENT,
                    Kind.PRODUCER -> applicationName
                    else -> {
                        platformHeaders[ApiHeaderNames.SERVICE_INITIATOR] ?: "unknown"
                    }
                }
            }
            is ReceiverContext<*> -> {
                when (context.kind) {
                    Kind.SERVER,
                    Kind.CONSUMER -> platformHeaders[ApiHeaderNames.SERVICE_INITIATOR] ?: "unknown"
                    else -> {
                        applicationName
                    }
                }
            }
            else -> {
                applicationName
            }
        }
    }
}