rootProject.name = "jvm-backend-platform"

include(
    "core:api",
    "core:api-exception-handler",
    "core:exception",
    "core:async",
    "core:build-info",
    "core:environment",
    "core:extensions",
    "core:http-server",
    "core:metrics",
    "core:open-api",
    "core:service",
    "core:spring",
    "core:tracing",
    "core:validation",

    "core:logging",
    "core:logging-sl4j",

    "core:starter",

    "db:core",

    "integration:http",
    "integration:kafka",
    "integration:s3-minio",

    "security:api-key",
    "security:oauth2-resource"
)

pluginManagement {
    repositories {
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven {
            url = uri(System.getenv("NEXUS_URL") ?: "")
        }
    }
    versionCatalogs {
        create("libs") {

            /////////////////////////////
            // REGION OPEN SOURCE LIBS //
            /////////////////////////////

            val kotlinVersion = "1.9.24"
            val kotlinxVersion = "1.8.1"
            val springVersion = "6.1.10"
            val springBootVersion = "3.3.2"
            val springDependencyManagementVersion = "1.1.6"

            // base plugin
            plugin("spring-boot", "org.springframework.boot").version(springBootVersion)
            plugin("spring-dependency-management", "io.spring.dependency-management").version(springDependencyManagementVersion)
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlinVersion)
            plugin("kotlin-kapt", "org.jetbrains.kotlin.kapt").version(kotlinVersion)
            plugin("kotlin-plugin-spring", "org.jetbrains.kotlin.plugin.spring").version(kotlinVersion)

            // base kotlin
            library("kotlin-reflect", "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
            library("kotlin-stdlib-jdk8", "org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")
            library("kotlinx-coroutines-core", "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxVersion")

            // base spring boot
            library("spring-boot-starter", "org.springframework.boot:spring-boot-starter:$springBootVersion")
            library("spring-boot-autoconfigure", "org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
            library("spring-boot-configuration-processor", "org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
            library("spring-boot-validation", "org.springframework.boot:spring-boot-starter-validation:$springBootVersion")

            // base spring http server
            val jakartaServletApiVersion = "6.1.0"
            library("spring-web", "org.springframework:spring-web:$springVersion")
            library("spring-webmvc", "org.springframework:spring-webmvc:$springVersion")
            // TODO перейти на undertow когда будет поддержка virtual threads с 3.4.0
            library("spring-boot-web", "org.springframework.boot:spring-boot-starter-web:$springBootVersion")
            library("jakarta-servlet-api", "jakarta.servlet:jakarta.servlet-api:$jakartaServletApiVersion")

            // base spring security
            library("spring-boot-starter-oauth2-resource-server", "org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion")

            // base database
            val postgresJdbcVersion = "42.7.3"
            val dataSourceMicrometerVersion = "1.0.5"
            library("spring-boot-jdbc", "org.springframework.boot:spring-boot-starter-data-jdbc:$springBootVersion")
            library("datasource-micrometer", "net.ttddyy.observation:datasource-micrometer-spring-boot:$dataSourceMicrometerVersion")
            library("postgres-jdbc", "org.postgresql:postgresql:$postgresJdbcVersion")

            val flywayVersion = "10.16.0"
            library("flyway", "org.flywaydb:flyway-core:$flywayVersion")
            library("flyway-postgresql", "org.flywaydb:flyway-database-postgresql:$flywayVersion")

            // base kafka
            val springKafkaVersion = "3.2.2"
            library("spring-kafka", "org.springframework.kafka:spring-kafka:$springKafkaVersion")

            // base actuator
            library("spring-boot-actuator", "org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")

            // base prometheus
            val micrometerVersion = "1.13.2"
            library("micrometer", "io.micrometer:micrometer-core:$micrometerVersion")
            library("prometheus", "io.micrometer:micrometer-registry-prometheus:$micrometerVersion")

            // base tracing
            val micrometerTracingVersion = "1.3.2"
            val otlpExporterVersion = "1.40.0"
            library("micrometer-tracing", "io.micrometer:micrometer-tracing:$micrometerTracingVersion")
            library("micrometer-tracing-bridge-otel", "io.micrometer:micrometer-tracing-bridge-otel:$micrometerTracingVersion")
            library("opentelemetry-exporter-otlp", "io.opentelemetry:opentelemetry-exporter-otlp:$otlpExporterVersion")

            // base open-api
            val springdocVersion = "2.6.0"
            val swaggerAnnotationsVersion = "2.2.22"
            library("springdoc-openapi-common", "org.springdoc:springdoc-openapi-starter-common:$springdocVersion")
            library("springdoc-openapi-webmvc-ui", "org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
            library("swagger-annotations", "io.swagger.core.v3:swagger-annotations:$swaggerAnnotationsVersion")

            // base async-api
            val springwolfVersion = "1.4.0"
            library("springwolf-generic-binding", "io.github.springwolf:springwolf-generic-binding:$springwolfVersion")
            library("springwolf-ui", "io.github.springwolf:springwolf-ui:$springwolfVersion")

            // base jackson
            val jacksonVersion = "2.17.2"
            library("jackson-module-kotlin", "com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
            library("jackson-datatype-jsr310", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
            library("jackson-databind", "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

            // base log
            val slf4jVersion = "2.0.13"
            library("slf4j", "org.slf4j:slf4j-api:$slf4jVersion")
            library("slf4j-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinxVersion")

            val logbookVersion = "3.9.0"
            library("logbook-spring", "org.zalando:logbook-spring-boot-autoconfigure:$logbookVersion")
            library("logbook-json", "org.zalando:logbook-json:$logbookVersion")

            val logstashLogbackEncoderVersion = "7.4"
            library("logstash-logback-encoder", "net.logstash.logback:logstash-logback-encoder:$logstashLogbackEncoderVersion")

            // base common
            val monetaVersion = "1.4.4"
            val jacksonDatatypeMoneyVersion = "1.3.0"
            library("moneta", "org.javamoney:moneta:$monetaVersion")
            // Доп зависимость чтобы работала сериализация Money в JSONB
            library("jackson-datatype-money", "org.zalando:jackson-datatype-money:$jacksonDatatypeMoneyVersion")

            val icu4jVersion = "75.1"
            library("icu4j", "com.ibm.icu:icu4j:$icu4jVersion")

            val javaUuidGeneratorVersion = "5.1.0"
            library("java-uuid-generator", "com.fasterxml.uuid:java-uuid-generator:$javaUuidGeneratorVersion")

            // s3-minio
            val minioVersion = "8.5.11"
            library("minio", "io.minio:minio:$minioVersion")

            // http
            val apacheHttpClientVersion = "5.3.1"
            library("http-client", "org.apache.httpcomponents.client5:httpclient5:$apacheHttpClientVersion")

            // base test libs
            val junitJupiterEngineVersion = "5.10.3"
            val kotestVersion = "5.9.1"
            val mockkVersion = "1.13.12"
            library("spring-boot-test", "org.springframework.boot:spring-boot-starter-test:$springBootVersion")
            library("junit-jupiter-engine", "org.junit.jupiter:junit-jupiter-engine:$junitJupiterEngineVersion")
            library("kotest", "io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
            library("mockk", "io.mockk:mockk:$mockkVersion")
            library("kotlin-test-junit", "org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")

            /////////////////////////////////
            // REGION PLATFORM LIBS //
            /////////////////////////////////

            // base core libs
            val platformGroup = "ru.nemodev.platform"
            val platformVersion = "1.0.0"

            library("core-api", "$platformGroup:core-api:$platformVersion")
            library("core-api-exception-handler", "$platformGroup:core-api-exception-handler:$platformVersion")
            library("core-exception", "$platformGroup:core-exception:$platformVersion")
            library("core-async", "$platformGroup:core-async:$platformVersion")
            library("core-build-info", "$platformGroup:core-build-info:$platformVersion")
            library("core-environment", "$platformGroup:core-environment:$platformVersion")
            library("core-extensions", "$platformGroup:core-extensions:$platformVersion")
            library("core-http-server", "$platformGroup:core-http-server:$platformVersion")
            library("core-metrics", "$platformGroup:core-metrics:$platformVersion")
            library("core-open-api", "$platformGroup:core-open-api:$platformVersion")
            library("core-service", "$platformGroup:core-service:$platformVersion")
            library("core-spring", "$platformGroup:core-spring:$platformVersion")
            library("core-tracing", "$platformGroup:core-tracing:$platformVersion")
            library("core-validation", "$platformGroup:core-validation:$platformVersion")
            library("core-logging", "$platformGroup:core-logging:$platformVersion")
            library("core-logging-sl4j", "$platformGroup:core-logging-sl4j:$platformVersion")

            // core database libs
            library("core-db", "$platformGroup:core-db:$platformVersion")

            // core integration libs
            library("core-integration-http", "$platformGroup:core-integration-http:$platformVersion")
            library("core-integration-kafka", "$platformGroup:core-integration-kafka:$platformVersion")
            library("core-integration-s3-minio", "$platformGroup:core-integration-s3-minio:$platformVersion")

            // core security libs
            library("core-security-api-key", "$platformGroup:core-security-api-key:$platformVersion")
            library("core-security-oauth2-resource", "$platformGroup:core-security-oauth2-resource:$platformVersion")


            /////////////////////////////////
            // REGION LIB-BUNDLES ///////////
            /////////////////////////////////

            // base/core bundles
            bundle("core-starter", listOf(
                "core-api",
                "core-api-exception-handler",
                "core-exception",
                "core-async",
                "core-build-info",
                "core-environment",
                "core-extensions",
                "core-http-server",
                "core-metrics",
                "core-open-api",
                "core-service",
                "core-spring",
                "core-tracing",
                "core-validation",

                "core-logging",
                "core-logging-sl4j",

                "jackson-module-kotlin",
                "jackson-datatype-jsr310",
                "jackson-databind",
            ))

            bundle("base-metrics", listOf(
                "micrometer",
                "prometheus"
            ))

            bundle("base-tracing", listOf(
                "micrometer-tracing",
                "micrometer-tracing-bridge-otel",
                "opentelemetry-exporter-otlp",
            ))

            bundle("base-springdoc", listOf(
                "springdoc-openapi-common",
                "springdoc-openapi-webmvc-ui"
            ))

            bundle("base-springwolf", listOf(
                "springwolf-generic-binding",
                "springwolf-ui"
            ))

            bundle("base-jdbc", listOf(
                "spring-boot-jdbc",
                "datasource-micrometer",
                "postgres-jdbc",
            ))

            bundle("base-flyway", listOf(
                "postgres-jdbc",
                "flyway",
                "flyway-postgresql",
            ))

            bundle("base-jackson", listOf(
                "jackson-module-kotlin",
                "jackson-datatype-jsr310",
                "jackson-databind"
            ))

            bundle("kotlin", listOf(
                "kotlin-reflect",
                "kotlin-stdlib-jdk8",
                "kotlinx-coroutines-core"
            ))

            bundle("tests", listOf(
                "junit-jupiter-engine",
                "kotest",
                "mockk",
                "kotlin-test-junit"
            ))

            bundle("tests-spring", listOf(
                "spring-boot-test",
                "junit-jupiter-engine",
                "kotest",
                "mockk",
                "kotlin-test-junit"
            ))
        }
    }
}
