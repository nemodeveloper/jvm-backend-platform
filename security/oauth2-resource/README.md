# Библиотека предоставляет возможности
- 1 Объявление path-ов для авторизации
- 2 Указание ролей для аутентификации path

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-security-oauth2-resource")

## Changelogs

## Пример конфигурации application.yml
Более подробную информацию смотрите в файле настроек CoreSecurityOAuth2ResourceProperties

``` yaml
platform:
  security:
    oauth2-resource:
      enabled: true # глобальная настройка для отключения, по умолчанию true, можно явно не указывать
      rsa-key:
        public-key: ${RSA_PUBLIC_KEY:-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3FBb4anCnkvku2zaK1P7wF3Ixyk50qWQkP/1YhI2PP4rKcRrQySLTBGvJJ/08RwcUQ2aV09SvfVkViOTcNTH8TMVgMPsn0m7E0AF1aF7HOSCQxB/t5/K0duORrLQdhhIJkoFdoF2mDa5UNh/D1qGWi7EjkP6XBC+Lb5MjsJW0Gwa6f2O0PDfoklluUL9H9F1SIAPt/CohuBhFr2/xXgviHWkCBv66IfBEhZSjB0AGxAd+AyMpbLTCXP7kkwb9WmhNlDBaeSxwrfIH0s8uVhUiN4qkaPlATHRVW7Ux48KeAh/vTmoiCWwvP+O3FJ++KFjN+DMjiBRAJDlAjdGs6/zIQIDAQAB-----END PUBLIC KEY-----}
      auth-paths:
        - methods: GET            # авторизация всех GET методов, проверяется JWT токен
          path: /admin/v1/modules
        - methods: POST,PUT       # авторизация всех POST PUT методов, проверяется JWT токен + роли
          path: /admin/v1/modules
          roles: ADMIN,VIEWER
        - path: /v1/modules/**    # авторизация всех методов, проверяется JWT токен
        - path: /v1/modules/**    # авторизация всех методов, проверяется JWT токен + роли
          roles: ADMIN,VIEWER
```

## Настройка для Swagger
### Создание схемы bearerAuth
Для корректной отправки токена через Swagger необходимо указать схему для отправки авторизации
 ```kotlin
@AutoConfiguration
@SecurityScheme(
    name = "bearerAuth",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER
)
class OpenApiConfig {
    ...
}
```
### Подключение схемы brearAuth
Для того, чтобы можно было отправить правильно токен через Swagger над классом или методом, которые надо будет авторизовывать, необходимо добавить следующую аннотацию
#### Пример для класса
```kotlin
@RestController
@RequestMapping("/admin/v1/modules")
@SecurityRequirement(name = "bearerAuth")
class AdminController {
    ...
}
```
#### Пример для метода
```kotlin
@RestController
@RequestMapping("/admin/v1/modules")
class AdminController {
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    fun find(): Response {
        ...
    }
}
```

## Получение и работа JWT в коде
Чтобы получить JWT токен в запросе и работать с ним как с объектом необходимо, необходимо сделать следующее:
- Добавить параметр типа JWT
- Добавить к нему аннотацию @AuthenticationPrincipal
- Скрыть параметр для swagger
### Пример
```kotlin
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@RestController
@RequestMapping("/admin/v1/modules")
class AdminController {
    @GetMapping
    @SecurityRequirement(name = "bearerAuth")
    fun find(
        @Parameter(description = "Токен авторизации", required = true, hidden = true)
        @AuthenticationPrincipal 
        token: Jwt
    ): Response {
        ...
    }
}
```