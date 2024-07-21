package ru.nemodev.platform.core.logging.logbook

import org.slf4j.MDC
import org.zalando.logbook.CorrelationId
import org.zalando.logbook.HttpRequest
import ru.nemodev.platform.core.api.headers.ApiHeaderNames
import ru.nemodev.platform.core.service.generator.IdGeneratorService

class LogbookCorrelationId(
    private val idGeneratorService: IdGeneratorService
) : CorrelationId {
    override fun generate(request: HttpRequest): String {
        return request.headers[ApiHeaderNames.REQUEST_ID]?.firstOrNull()
            ?: MDC.get(ApiHeaderNames.REQUEST_ID)
            ?: idGeneratorService.generateId()
    }
}