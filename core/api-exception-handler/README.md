# Библиотека предоставляет возможности
- 1 Обработка исключений в едином стиле на основе библиотеки core-exception

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-api-exception-handler")

## Changelogs

## Конфигурация application.yml
``` yml
platform:
  core:
    api-exception-handler:
      error-fields: # маппинг внутренних ошибок полей валидации на те что отдаюся в api, используется только для 400 ошибок при ошибках spring-validation, маппинг по дефолту смотри в CoreApiExceptionHandlerProperties
        - internal-code: Foo
          external-code: FOO
```

## Использование
- 1 Обработка исключений реализована в классе ApiExceptionHandler