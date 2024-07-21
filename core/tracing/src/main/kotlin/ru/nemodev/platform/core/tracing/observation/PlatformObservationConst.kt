package ru.nemodev.platform.core.tracing.observation

import io.opentelemetry.context.ContextKey
import ru.nemodev.platform.core.api.headers.ApiHeaderNames

object PlatformObservationConst {

    val platformHeadersContextKey = ContextKey.named<Map<String, String>>("platform-headers")!!

    val platformObservationHeaders = listOf(
        ApiHeaderNames.REQUEST_ID,
        ApiHeaderNames.USER_ID,
        ApiHeaderNames.DEBUG_MODE,
        ApiHeaderNames.SERVICE_INITIATOR
    )

    val platformPropagationHeaders = listOf(
        ApiHeaderNames.REQUEST_ID,
        ApiHeaderNames.DEBUG_MODE,
        ApiHeaderNames.SERVICE_INITIATOR
    )
}