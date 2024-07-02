package ru.nemodev.platform.core.logging.constant

object PathFilterPatterns {

    val default = setOf(
        "/",
        "**/favicon.ico",
        "/favicon.ico",
        "**/csrf",
        "/csrf",
        "**/webjars**",
        "/webjars**",
        "**/swagger-ui.html**",
        "/swagger-ui.html**",
        "**/swagger-ui**",
        "/swagger-ui**",
        "**/v*/api-docs**",
        "/v*/api-docs**",
        "**/configuration/ui",
        "/configuration/ui",
        "**/configuration/security",
        "/configuration/security",
        "**/swagger-resources**",
        "/swagger-resources**",
        "**/open-api**",
        "/open-api**",
        "**/actuator**",
        "/actuator**",
        "**/springwolf**",
        "/springwolf**",
    )
}
