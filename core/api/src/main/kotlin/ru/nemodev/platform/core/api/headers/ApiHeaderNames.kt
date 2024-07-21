package ru.nemodev.platform.core.api.headers

object ApiHeaderNames {
    const val USER_ID = "x-user-id"
    const val SERVICE_INITIATOR = "x-service-initiator"
    const val REQUEST_ID = "x-request-id"
    const val DEBUG_MODE = "x-debug-mode"       // режим отладки, если передать true будут включены логи запросов/ответов
    const val API_KEY = "x-api-key"
}