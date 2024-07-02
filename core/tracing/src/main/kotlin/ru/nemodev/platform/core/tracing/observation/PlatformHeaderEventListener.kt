package ru.nemodev.platform.core.tracing.observation

import io.micrometer.tracing.otel.bridge.EventListener
import io.micrometer.tracing.otel.bridge.EventPublishingContextWrapper
import io.opentelemetry.context.Context
import org.slf4j.MDC
import ru.nemodev.platform.core.extensions.getPrivateField

class PlatformHeaderEventListener : EventListener {

    override fun onEvent(event: Any) {
        when (event) {
            is EventPublishingContextWrapper.ScopeAttachedEvent -> {
                onScopeAttached(event)
            }

            is EventPublishingContextWrapper.ScopeRestoredEvent -> {
                onScopeRestored(event)
            }

            is EventPublishingContextWrapper.ScopeClosedEvent -> {
                onScopeClosed()
            }
        }
    }

    private fun onScopeAttached(event: EventPublishingContextWrapper.ScopeAttachedEvent) {
        // К сожалению на текущий момент получить context из события по другому нельзя
        // Context наполняется из заголовков в PlatformHeaderTextMapPropagator
        val platformHeaderContext = event.getPrivateField<Context>("context")
            ?.get(PlatformObservationConst.platformHeadersContextKey)
            ?: return

        PlatformObservationConst.platformPropagationHeaders.forEach { headerName ->
            platformHeaderContext[headerName]?.let {
                MDC.put(headerName, it)
            }
        }
    }

    private fun onScopeRestored(event: EventPublishingContextWrapper.ScopeRestoredEvent) {
        val platformHeaderContext = event.getPrivateField<Context>("context")
            ?.get(PlatformObservationConst.platformHeadersContextKey)
            ?: return

        PlatformObservationConst.platformPropagationHeaders.forEach { headerName ->
            platformHeaderContext[headerName]?.let {
                MDC.put(headerName, it)
            }
        }
    }

    private fun onScopeClosed() {
        PlatformObservationConst.platformPropagationHeaders.forEach {
            MDC.remove(it)
        }
    }
}