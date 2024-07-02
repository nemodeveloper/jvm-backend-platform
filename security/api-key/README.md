# Библиотека предоставляет возможности
- 1 Объявление path-ов для авторизации по api-key

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-security-api-key")

## Changelogs

## Пример конфигурации application.yml
Более подробную информацию смотрите в файле настроек CoreSecurityApiKeyProperties

``` yaml
platform:
  security:
    api-key: # конфигурация api-key для контроллеров
      enabled: true # включить/выключить авторизацию по api-key
      key: ${API_KEY:55298707-117c-44a5-8434-162fb9d2e4c2} # ключ api который будет сравниваться со сзначением в заголовке запроса api-key
      cors:
        enabled: false # разрешает принимать предварительный запрос и дает доступ ко всем методам и заголовкам
      authPaths: 
        - method: GET           # авторизация GET методов, значение по-умолчанию пустой список
          path: v1/accounts     # авторизация методов содержащих "v1/accounts"
        - path: v1/operations   # авторизация методов содержащих "v1/operations"
```

## Настройка для Swagger
### Создание схемы apiKeyAuth
Для корректной отправки api-key через Swagger необходимо указать схему для отправки авторизации
 ```kotlin
@AutoConfiguration
@SecurityScheme(
    name = "apiKeyAuth",
    type = SecuritySchemeType.APIKEY,
    `in` = SecuritySchemeIn.HEADER,
    paramName = ApiHeaderNames.API_KEY
)
class OpenApiConfig {
    ...
}
```
### Подключение схемы apiKeyAuth
Для того, чтобы можно было отправить правильно api-key через Swagger над классом или методом, которые надо будет авторизовывать, необходимо добавить следующую аннотацию
#### Пример для класса
```kotlin
@RestController
@RequestMapping("/v1/accounts")
@SecurityRequirement(name = "apiKeyAuth")
class AdminController {
    ...
}
```
#### Пример для метода
```kotlin
@RestController
@RequestMapping("/v1/accounts")
class AdminController {
    @GetMapping
    @SecurityRequirement(name = "apiKeyAuth")
    fun find(): Response {
        ...
    }
}
```

## Получение и работа Api-Key в коде
Чтобы получить api-key токен в запросе и работать с ним как с объектом, необходимо сделать следующее:
- Добавить параметр типа String
- Добавить к нему аннотацию @RequestHeader(ApiHeaderNames.API_KEY)
- Скрыть параметр для swagger
### Пример
```kotlin
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@RestController
@RequestMapping("/v1/accounts")
@SecurityRequirement(name = "apiKeyAuth")
class AdminController {
    @GetMapping
    fun find(
        @Parameter(description = "api-key", example = "55298707-117c-44a5-8434-162fb9d2e4c2", required = true)
        @RequestHeader(ApiHeaderNames.API_KEY)
        apiKey: String
    ): Response {
        ...
    }
}
```