package ru.nemodev.platform.core.buildinfo.service

interface BuildInfoService {
    fun getPlatformSdkVersion(): String
}

class BuildInfoServiceImpl : BuildInfoService {

    private val innerPlatformSdkVersion = runCatching {
        BuildInfoServiceImpl::class.java.`package`.implementationVersion ?: "unknown"
    }.getOrElse { "unknown" }

    override fun getPlatformSdkVersion() = innerPlatformSdkVersion
}