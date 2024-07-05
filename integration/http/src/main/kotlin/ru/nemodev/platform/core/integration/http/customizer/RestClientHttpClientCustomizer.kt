package ru.nemodev.platform.core.integration.http.customizer

import org.apache.hc.client5.http.auth.AuthScope
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials
import org.apache.hc.client5.http.config.ConnectionConfig
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory
import org.apache.hc.client5.http.ssl.TrustAllStrategy
import org.apache.hc.core5.http.ConnectionClosedException
import org.apache.hc.core5.http.HttpHost
import org.apache.hc.core5.http.HttpRequest
import org.apache.hc.core5.ssl.SSLContextBuilder
import org.apache.hc.core5.util.TimeValue
import org.apache.hc.core5.util.Timeout
import org.springframework.http.HttpMethod
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestClient
import ru.nemodev.platform.core.integration.http.config.RestClientProperties
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.NoRouteToHostException
import java.net.UnknownHostException
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException

class RestClientHttpClientCustomizer : RestClientPropertyCustomizer {

    companion object {
        private val insecureSSLContext = SSLContextBuilder.create()
            .loadTrustMaterial(null, TrustAllStrategy())
            .build()

        private val retryExceptions = setOf(
            InterruptedIOException::class.java,
            UnknownHostException::class.java,
            ConnectException::class.java,
            ConnectionClosedException::class.java,
            NoRouteToHostException::class.java,
            SSLException::class.java
        )

        private const val HTTP_POOL_MAX_CONNECTION = 100
        private val idleConnectionTimeout = TimeValue.ofMinutes(3)
    }

    override fun customize(builder: RestClient.Builder, properties: RestClientProperties) {
        val httpClientBuilder = HttpClientBuilder.create()

        properties.proxy?.let { proxy ->
            httpClientBuilder.setProxy(
                HttpHost(proxy.host, proxy.port)
            )
            if (proxy.username != null && proxy.password != null) {
                httpClientBuilder.setDefaultCredentialsProvider(
                    BasicCredentialsProvider().apply {
                        setCredentials(
                            AuthScope(proxy.host, proxy.port),
                            UsernamePasswordCredentials(
                                proxy.username, proxy.password.toCharArray()
                            )
                        )
                    }
                )
            }
            httpClientBuilder.disableCookieManagement()
        }

        builder.requestFactory(
            HttpComponentsClientHttpRequestFactory(
                httpClientBuilder
                    .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create()
                            .setDefaultConnectionConfig(
                                ConnectionConfig.custom()
                                    .setConnectTimeout(Timeout.of(properties.timeout.connection))
                                    .build()
                            )
                            .setSSLSocketFactory(
                                SSLConnectionSocketFactory(
                                    if (properties.ssl == null) insecureSSLContext
                                    else buildSSLContext(properties.ssl)
                                )
                            )
                            .setMaxConnTotal(HTTP_POOL_MAX_CONNECTION)
                            .build()
                    )
                    .evictIdleConnections(idleConnectionTimeout)
                    .evictExpiredConnections()
                    .setDefaultRequestConfig(
                        RequestConfig.custom()
                            .setConnectionRequestTimeout(Timeout.of(properties.timeout.connection))
                            .setResponseTimeout(Timeout.of(properties.timeout.read))
                            .setRedirectsEnabled(properties.redirectEnabled)
                            .build()
                    ).apply {
                        if (!properties.retry.enabled) {
                            disableAutomaticRetries()
                        } else {
                            setRetryStrategy(
                                object : DefaultHttpRequestRetryStrategy(
                                    properties.retry.maxAttempts,
                                    TimeValue.of(properties.retry.delay),
                                    retryExceptions,
                                    properties.retry.statusCodes
                                ) {
                                    override fun handleAsIdempotent(request: HttpRequest): Boolean {
                                        return if (properties.retry.methods.isEmpty()) {
                                            request.method.equals(HttpMethod.GET.name(), ignoreCase = true)
                                        } else {
                                            HttpMethod.valueOf(request.method.uppercase()) in properties.retry.methods
                                        }
                                    }
                                }
                            )
                        }
                    }
                    .build()
            )
        )
    }

    private fun buildSSLContext(sslProperties: RestClientProperties.SSL): SSLContext {
        return SSLContextBuilder()
            .loadKeyMaterial(
                sslProperties.keystoreLocation!!.file,
                sslProperties.keystorePassword?.toCharArray() ?: "".toCharArray(),
                sslProperties.keyPassword?.toCharArray() ?: "".toCharArray()
            )
            .loadTrustMaterial(
                sslProperties.truststoreLocation!!.file,
                sslProperties.truststorePassword?.toCharArray() ?: "".toCharArray()
            )
            .build()
    }

}