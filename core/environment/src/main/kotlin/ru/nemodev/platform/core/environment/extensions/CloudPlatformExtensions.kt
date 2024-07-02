package ru.nemodev.platform.core.environment.extensions

import org.springframework.boot.cloud.CloudPlatform

fun CloudPlatform.isK8S() = this == CloudPlatform.KUBERNETES