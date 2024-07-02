package ru.nemodev.platform.core.security.apikey.config

import jakarta.servlet.Filter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import ru.nemodev.platform.core.api.headers.ApiHeaderNames

@AutoConfiguration
@EnableConfigurationProperties(SecurityApiKeyProperties::class)
@ConditionalOnProperty(prefix = "platform.security.api-key", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class ApiKeyConfig(
    private val properties: SecurityApiKeyProperties
) {

    companion object {
        private const val ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME = "Access-Control-Allow-Origin"
        private const val ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME = "Access-Control-Allow-Methods"
        private const val ACCESS_CONTROL_ALLOW_HEADERS_HEADER_NAME = "Access-Control-Allow-Headers"
        private const val ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER_NAME = "Access-Control-Allow-Credentials"
    }

    @Bean
    fun apiKeyFilter(): Filter = Filter { request, response, chain ->
        if (request is HttpServletRequest && response is HttpServletResponse) {
            if (properties.cors.enabled && request.method == HttpMethod.OPTIONS.name()) {
                response.addHeader(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER_NAME, "*")
                response.addHeader(ACCESS_CONTROL_ALLOW_METHODS_HEADER_NAME, "*")
                response.addHeader(ACCESS_CONTROL_ALLOW_HEADERS_HEADER_NAME, "*")
                response.addHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER_NAME, "false")
                return@Filter
            }
            if (pathMatcher(request) && properties.key != request.getHeader(ApiHeaderNames.API_KEY)) {
                response.status = HttpStatus.UNAUTHORIZED.series().value()
                return@Filter
            }
        } else {
            chain.doFilter(request, response)
        }
    }

    private fun pathMatcher(request: HttpServletRequest): Boolean {
        properties.authPaths.forEach { authPath ->
            val result = if (!authPath.methods.isNullOrEmpty()) {
                request.contextPath.contains(authPath.path) && authPath.methods.contains(HttpMethod.valueOf(request.method))
            } else {
                request.contextPath.contains(authPath.path)
            }
            if (result) return true
        }
        return false
    }
}