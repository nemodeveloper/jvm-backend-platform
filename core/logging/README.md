# Библиотека предоставляет возможности
- 1 Логирование HTTP запросов/ответов
- 1.1 Продвинутые HTTP логи
- 1.2 Маскирование заголовков
- 2 Логирование построено на библиотеке Logbook
- 2.1 Более подробно смотри - https://github.com/zalando/logbook

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-logging")

## Changelogs

## Пример конфигурации application.yml
Задавать настройки platform.core.logging.* не нужно они заданы по умолчанию, переопределяйте их по мере необходимости
Более подробную информацию смотри в LoggingProperties

``` yaml
platform:
  core:
    logging:
      console-enabled: ${PLATFORM_CORE_LOGGING_CONSOLE_ENABLED:true}
      message-max-size: ${PLATFORM_CORE_LOGGING_MESSAGE_MAX_SIZE:128KB}
      format: ${PLATFORM_CORE_LOGGING_FORMAT:plain}
      body:
        format: JSON_PRETTY
        request-exclude-body-patterns: ""
        response-exclude-body-patterns: ""
        masking:
          json-paths:
            - path: "$..accountNumber"
            - path: '$..[?(@.key == "ACCOUNT_NUMBER")].value'
      masking: ""
      log-packages: "ru.nemodev"
      
logbook:
  # смотри LogbookProperties
      
# Настройки ниже задаются по умолчанию в core-starter
logging:
  pattern:
    console: "%clr(%d{${LOG_DATE_FORMAT_PATTERN:yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:%5p}) %clr(${PID:- }){magenta} %clr(---){faint} %clr([%15.15t]){faint} %clr(%-40.40logger{40}){cyan} %clr(MDC={%X}){magenta} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:%wEx}"
  level:
    root: info # базовый уровень логирования МСа
    org.zalando.logbook: trace
    ru.nemodev.platform.core.logging: trace # включение логирования HTTP запросов, если указать уровень выше логи пропадут
```

## 1.1 - Продвинутые HTTP логи
Класс LogbookHttpLogFormatter
- Логирует запросы/ответы под уровнем TRACE
- В рамках одного лога логирует весь запрос/ответ
- Если в запросе сервиса передать заголовок x-log-mode=true, будет принудительно включено логирование указанных пакетов в log-packages
- Используйте platform.core.logging.format: JSON_COMPACT для корректного отображения логов в Grafana
