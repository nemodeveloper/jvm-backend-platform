# Библиотека предоставляет возможности
- 1 Базовые классы исключений

## Подключение библиотеки gradle 

    implementation("ru.nemodev.platform:core-exception")

## Changelogs

## 1 - Работа с исключениями
- 1.1 Смотри классы LogicException / CriticalException и их наследники
- 1.2 При работе в паре с библиотекой core-api-exception-handler исключения будут автоматически преобразоваться в универсальный ответ с ошибкой с указанным http статус кодом из исключения

## Генерация базовых исключений на примере сущности платеж(payment)

``` kotlin
throw NotFoundLogicalException(
    errorCode = ErrorCode.create(code = "NOT_FOUND", description = "Платеж не найден")
)
```

``` kotlin
throw ForbiddenLogicalException(
    errorCode = ErrorCode.create(code = "ACCESS_DENIED", description = "Доступ к платежу запрещен")
)
```

``` kotlin
throw ValidationLogicException(
    errorCode = ErrorCode.create(code = "VALIDATION_ERROR", description = "Ошибка валидации запроса"),
    errorFields = listOf(
        ErrorField.create(
            key = "summa",
            code = "MIN_SUMMA_ERROR",
            description = "Минимальная сумма платежа должна быть 50000 рублей"
        )
    )
)
```

``` kotlin
throw IncorrectParamsLogicException(
    errorCode = ErrorCode.create(code = "INCORRECT_PARAMS_ERROR", description = "Запрос содержит некорректные данные"),
    errorFields = listOf(
        ErrorField.create(
            key = "PaymentId",
            code = "BAD_REQUEST",
            description = "Запрос не содержит корректных данных"
        )
    )
)
```

``` kotlin
throw IntegrationCriticalException(
    service = "PARTHER_BANK",
    errorCode = ErrorCode.create(code = "SERVICE_TIMEOUT", description = "Сервис банка партнера не отвечает"),
    errorFields = listOf(
        ErrorField.create(
            key = "summa",
            code = "MIN_SUMMA_ERROR",
            description = "Минимальная сумма платежа должна быть 50000 рублей"
        )
    )
)