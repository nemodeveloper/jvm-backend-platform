# Библиотека предоставляет возможности
- 1 Подключение всех базовых библиотек для работы МС

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-starter")

## Changelogs


## Конфигурация
- 1 application.yml - по умолчанию стартер задает следующий базовый конфиг
``` yaml
spring:
  application:
    developed-by: Nemodev
  web:
    base-path: /${spring.application.name}
  jackson:
    default-property-inclusion: non_null
    deserialization:
      fail-on-unknown-properties: false
      read-unknown-enum-values-using-default-value: true
  docker:
    compose:
      enabled: false
  threads:
    virtual:
      enabled: true

management:
  endpoints:
    web:
      exposure:
        include: health, info, prometheus
  endpoint:
    health:
      show-details: always
      showComponents: always
      probes:
        enabled: true
      group:
        readiness:
          include: readinessState
        liveness:
          include: livenessState

server:
  shutdown: graceful
  max-http-request-header-size: 64KB
  http2:
    enabled: true
  error:
    whitelabel:
      enabled: false

logging:
  pattern:
    console: "%clr(%d{${LOG_DATE_FORMAT_PATTERN:yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{40}){cyan} %clr(MDC={%X}){magenta} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}"
  level:
    root: info
    ru.nemodev.platform.core.logging: trace
    ru.nemodev.platform.core.integration.kafka.logging: trace

```
- 2 стартер транзитивно подключает набор core/* библиотек