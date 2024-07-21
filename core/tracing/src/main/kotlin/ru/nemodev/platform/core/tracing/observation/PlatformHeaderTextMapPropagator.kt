package ru.nemodev.platform.core.tracing.observation

import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.TextMapGetter
import io.opentelemetry.context.propagation.TextMapPropagator
import io.opentelemetry.context.propagation.TextMapSetter
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.platform.core.service.generator.IdGeneratorService

class PlatformHeaderTextMapPropagator(
    private val applicationName: String,
    private val idGeneratorService: IdGeneratorService,
) : TextMapPropagator {

    override fun fields() = PlatformObservationConst.platformPropagationHeaders

    override fun <C : Any?> inject(context: Context, carrier: C?, setter: TextMapSetter<C>) {
        val platformHeadersContext = context.get(PlatformObservationConst.platformHeadersContextKey)
        if (platformHeadersContext.isNullOrEmpty()) {
            return
        }
        PlatformObservationConst.platformPropagationHeaders.forEach { headerName ->
            when (headerName) {
                ApiHeaderNames.SERVICE_INITIATOR -> {
                    setter.set(carrier, headerName, applicationName)
                }
                else -> {
                    platformHeadersContext[headerName]?.let {
                        setter.set(carrier, headerName, it)
                    }
                }
            }
        }
    }

    override fun <C : Any?> extract(context: Context, carrier: C?, getter: TextMapGetter<C>): Context {
        val platformHeadersContext = mutableMapOf<String, String>()
        PlatformObservationConst.platformObservationHeaders.forEach { headerName ->
            val headerValue = getter.get(carrier, headerName)
            if (!headerValue.isNullOrEmpty()) {
                platformHeadersContext[headerName] = headerValue
            }
        }

        if (platformHeadersContext[ApiHeaderNames.REQUEST_ID] == null) {
            platformHeadersContext[ApiHeaderNames.REQUEST_ID] = idGeneratorService.generateUUID().toString()
        }

        return context.with(
            PlatformObservationConst.platformHeadersContextKey,
            platformHeadersContext
        )
    }
}