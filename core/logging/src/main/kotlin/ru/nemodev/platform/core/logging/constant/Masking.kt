package ru.nemodev.platform.core.logging.constant

import org.slf4j.Marker
import org.slf4j.MarkerFactory
import ru.nemodev.platform.core.api.headers.ApiHeaderNames

object Masking {
    val MASKING_MARKER: Marker = MarkerFactory.getMarker("masking")

    val maskingHeaders = setOf(
        ApiHeaderNames.API_KEY
    )
}
