package ru.nemodev.platform.core.api.headers

object ApiHeaderNames {
    const val USER_ID = "x-user-id"
    const val SERVICE_INITIATOR = "x-service-initiator"
    const val REQUEST_ID = "x-request-id"
    const val LOG_MODE = "x-log-mode"       // режим форсированного логирования, если передать true будут включены логи запросов/ответов
    const val API_KEY = "x-api-key"
}