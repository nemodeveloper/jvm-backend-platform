package ru.nemodev.platform.core.tracing.observation

import io.micrometer.common.KeyValue
import io.micrometer.observation.Observation.Context
import io.micrometer.observation.ObservationFilter
import io.micrometer.observation.transport.Kind
import io.micrometer.observation.transport.ReceiverContext
import io.micrometer.observation.transport.SenderContext
import io.micrometer.tracing.Tracer
import io.micrometer.tracing.handler.TracingObservationHandler
import io.micrometer.tracing.otel.bridge.OtelTraceContext
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.platform.core.extensions.getPrivateField
import java.util.concurrent.atomic.AtomicReference

class PlatformHeaderObservationFilter(
    private val applicationName: String,
    tracer: Tracer
) : ObservationFilter {

    companion object {
        val platformHeaderHighCardinalityKeys = mapOf(
            ApiHeaderNames.REQUEST_ID to "platform.request.id",
            ApiHeaderNames.USER_ID to "platform.user.id",
            ApiHeaderNames.DEBUG_MODE to "platform.debug.mode"
        )
    }

    // Необходим для вычисления TraceContext
    private val mockTracingObservationHandler = TracingObservationHandler<Context> { tracer }

    private fun getPlatformHeadersContext(context: Context): Map<String, String?>? {
        // Быстрый способ получения контекста если не было thread switching
        val fastPlatformHeadersContext = io.opentelemetry.context.Context.current()
            ?.get(PlatformObservationConst.platformHeadersContextKey)
        if (fastPlatformHeadersContext != null) {
            return fastPlatformHeadersContext
        }

        val traceContext = mockTracingObservationHandler.getParentSpan(context)?.context()
            ?: return null
        if (traceContext !is OtelTraceContext) {
            return null
        }

        return traceContext
            // Метод context private-package поэтому можем получить только через рефлексию
            .getPrivateField<AtomicReference<io.opentelemetry.context.Context>>("otelContext")
            ?.get()
            ?.get(PlatformObservationConst.platformHeadersContextKey)
    }

    override fun map(context: Context): Context {
        // Context заполняется в PlatformHeaderTextMapPropagator
        val platformHeaders = getPlatformHeadersContext(context) ?: return context

        platformHeaderHighCardinalityKeys.forEach { header ->
            platformHeaders[header.key]?.let {
                context.addHighCardinalityKeyValue(
                    KeyValue.of(
                        header.value,
                        it
                    )
                )
            }
        }

        context.addHighCardinalityKeyValue(
            KeyValue.of(
                "platform.service.initiator",
                getInitiatorService(context, platformHeaders)
            )
        )

        return context
    }

    private fun getInitiatorService(context: Context, platformHeaders: Map<String, String?>): String {
        return when (context) {
            is SenderContext<*> -> {
                when (context.kind) {
                    Kind.CLIENT,
                    Kind.PRODUCER -> applicationName
                    else -> {
                        platformHeaders[ApiHeaderNames.SERVICE_INITIATOR] ?: PlatformObservationConst.UNKNOWN_TAG_VALUE
                    }
                }
            }
            is ReceiverContext<*> -> {
                when (context.kind) {
                    Kind.SERVER,
                    Kind.CONSUMER -> platformHeaders[ApiHeaderNames.SERVICE_INITIATOR] ?: PlatformObservationConst.UNKNOWN_TAG_VALUE
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