package ru.nemodev.platform.core.environment.domain

enum class ContourType {
    LOCAL,
    DEV,
    STAGE,
    PREPROD,
    LOAD_TEST,
    PROD,
    UNKNOWN;

    fun isLocal() = this == LOCAL
    fun isDev() = this == DEV
    fun isStage() = this == STAGE
    fun isPreProd() = this == PREPROD
    fun isLoadTest() = this == LOAD_TEST
    fun isProd() = this == PROD
    fun isUnknown() = this == UNKNOWN

    fun isK8S() = !isLocal() && !isUnknown()

    companion object {
        val contourTypeMap = entries.associateBy(ContourType::name)

        fun fromString(rawValue: String): ContourType {
            return contourTypeMap[rawValue.uppercase()]
                ?: UNKNOWN
        }
    }
}