# Библиотека трассировки
- 1 Трассировка http запросов server/client 
- 2 Трассировка kafka producer/consumer 
- 3 Трассировка DB запросов  
- 4 Кастомная трассировка событий в сервисе 

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-tracing")

## Changelogs


## Конфигурация
- 1 application.yml настройки ниже задаются по умолчанию для работы достаточно задать env TRACING_ENABLED и TRACING_OTLP_URL
``` yaml 
platform:
  core:
    tracing:
      spring-security-enabled: false
      base-api-enabled: false
management:
  tracing:
    enabled: ${TRACING_ENABLED:false}
    propagation:
      type: ${TRACING_PROPAGATION_TYPE:w3c}
      produce: ${TRACING_PROPAGATION_PRODUCE:w3c}
      consume: ${TRACING_PROPAGATION_CONSUME:w3c}
    sampling:
      probability: 1.0
  otlp:
    tracing:
      endpoint: ${TRACING_OTLP_URL:http://localhost:4317}
      timeout: ${TRACING_OTLP_TIMEOUT:10s}
```

- 1.1 - Создание собственного tracing события
``` kotlin
class FooService(
    private val observationRegistry: ObservationRegistry
) {
    fun bar() {
        observationRegistry
            .createNotStarted("foo logic")
            .lowCardinalityKeyValue("foo.type", "bar")
            .observe {
                // .. foo logic
            }
            // or suspend fun action
            .observeSuspend {
                // .. suspend foo logic
            }
    }
}
```