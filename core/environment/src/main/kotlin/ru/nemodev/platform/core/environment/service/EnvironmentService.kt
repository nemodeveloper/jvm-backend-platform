package ru.nemodev.platform.core.environment.service

import org.springframework.boot.cloud.CloudPlatform
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import ru.nemodev.platform.core.environment.domain.ContourType
import ru.nemodev.platform.core.environment.domain.Host
import java.net.InetAddress

interface EnvironmentService {
    fun getHost(): Host
    fun getCloudPlatform(): CloudPlatform
    fun getContourType(): ContourType
    fun getEnvironment(key: String): String?
}

class EnvironmentServiceImpl : EnvironmentService, ApplicationContextAware {

    companion object {
        const val ENVIRONMENT_ENV_KEY = "ENVIRONMENT"
    }

    private val host = Host(
        name = runCatching {
            InetAddress.getLocalHost().hostName ?: "localhost"
        }.getOrElse { "localhost" },
        ip = runCatching {
            InetAddress.getLocalHost().hostAddress ?: "127.0.0.1"
        }.getOrElse { "127.0.0.1" }
    )

    private var cloudPlatform = CloudPlatform.NONE

    private val contourType = ContourType.fromString(System.getenv(ENVIRONMENT_ENV_KEY) ?: "")

    override fun getHost() = host
    override fun getCloudPlatform() = cloudPlatform
    override fun getContourType() = contourType
    override fun getEnvironment(key: String): String? = System.getenv(key)

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        cloudPlatform = CloudPlatform.getActive(applicationContext.environment) ?: CloudPlatform.NONE
    }
}