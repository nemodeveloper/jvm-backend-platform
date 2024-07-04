# Библиотека предоставляет возможности
- 1 Автоматическая генерация спецификации в формате OpenAPI v3 
- 2 Генерация swagger ui
- 3 Библиотека реализована на основе SpringDoc. Документацию по разметке кода смотрим тут: https://springdoc.org/

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-open-api")

## Changelogs

## Конфигурация
- 1 - Параметры для генерации описания берутся из аннотаций RestController и Tag. Создавать бины не требуется.

- 1.1 - application.yml - по умолчанию стартер задает следующий базовый конфиг
``` yaml 
springdoc:
  override-with-generic-response: false
  api-docs:
    enabled: ${PLATFORM_OPEN_API_ENABLED:true}
  group-configs:
    - group: admin
      display-name: Admin API
      paths-to-match:
        - /admin/**
    - group: oauth
      display-name: OAuth API
      paths-to-match:
        - /**/oauth/**
    - group: client
      display-name: Client API
      paths-to-match:
        - /**/**
      paths-to-exclude:
        - /**/admin/**
        - /**/oauth/**
  open-api:
    info:
      title: Название моего API
      description: Название моего API
      version: v1
      contact:
        name: Симанов А.Н
        url: tg://simanovan
        email: nemodev@yandex.ru
    servers:
      - description: LOCAL
        url: ${SERVICE_LOCAL_URL:http://localhost:8080}
      - description: PROD
        url: ${SERVICE_PROD_URL:http://localhost:8080}
  swagger-ui:
    enabled: ${PLATFORM_OPEN_API_ENABLED:true}
    disable-swagger-default-url: true
    tags-sorter: alpha
    operations-sorter: alpha

# В своем сервисе переопределяем название API
springdoc:
  open-api:
    info:
      title: Название моего API
      description: Название моего API
```

- 1.2 - Описание Controller и DTO  
Значение в ApiResponse.ExampleObject.value – ссылка на файл с json-примером в ресурсах, начинается с символа '@'. 
``` kotlin
@RestController
@RequestMapping("/v1/pets", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Питомцы", description = "Api питомцев")
class PetController {
    @Operation(
        summary = "Список питомцев отсортированных по дате создания desc",
        responses = [
            ApiResponse(responseCode = "200", description = "Успешный ответ"),
            ApiResponse(responseCode = "400", description = "Не правильный формат запроса",
                content = [Content(
                schema = Schema(implementation = ErrorDtoRs::class),
                examples = [ExampleObject(value = "@swagger/examples/pets_response_error_400.json")]
                )]
            ),
            ApiResponse(responseCode = "422", description = "Ошибка валидации полей запроса",
                content = [Content(
                schema = Schema(implementation = ErrorDtoRs::class),
                examples = [ExampleObject(value = "@swagger/examples/pets_response_error_422.json")]
                )]
            ),
            ApiResponse(responseCode = "500", description = "Ошибка обработки запроса",
                content = [Content(
                schema = Schema(implementation = ErrorDtoRs::class),
                examples = [ExampleObject(value = "@swagger/examples/pets_response_error_500.json")]
                )]
            )
            // etc responces
        ]
    )
    @GetMapping
    fun findAll(
        // params
    ): PageDtoRs<PetDtoRs> {
        TODO()
    }
}
```

``` kotlin
@Schema(description = "Питомец")
data class PetDtoRs(
    @Schema(description = "Id питомца", example = "add38d78b25847f0a13189dd3bcf8df9", minLength = 32, maxLength = 36)
    val id: String,
    @Schema(description = "Кличка питомца", example = "Мурка", minLength = 1, maxLength = 64)
    val name: String,
    @Schema(description = "Тип питомец", example = "CAT")
    val type: PetTypeDto
)

@Schema(description = "Тип питомца")
enum class PetTypeDto {
    DOG,
    CAT,
    UNKNOWN
}
```

## Результат
- 1 - Библиотека автоматически конфигурирует выставление методов /open-api/v3 /open-api-ui , конфиг смотри в resources/core-open-api.yml
