# Библиотека предоставляет возможности
- 1 Валидация запросов/бизнес данных

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-validation")

## Changelogs


## Использование 
Библиотека строится на основе spring-boot-starter-validation
Примеры использования https://reflectoring.io/bean-validation-with-spring-boot/
Стандартные коды сообщений ошибок смотри в ValidationMessages.properties
Исключения валидации обрабатываются в модуле core-api-exception-handler

- 1 Валидация полей Dto
``` kotlin
data class UserGroupCreateDtoRq(
    @field:NotBlank(message = "{platform.NotEmpty.message}")
    val key: String,

    @field:NotBlank(message = "{platform.NotEmpty.message}")
    val description: String,

    @field:NotEmpty(message = "{platform.ListNotEmpty.message}")
    val userIds: Set<UUID>,
    
    @field:Valid
    val test: Test
)
data class Test(
    @field:NotBlank(message = "{platform.NotEmpty.message}")
    val test: String
)

@PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE])
@ResponseStatus(HttpStatus.CREATED)
suspend fun create(
    @Valid
    @RequestBody
    userGroupCreateDtoRq: UserGroupCreateDtoRq
): UserGroupCreateDtoRs {
    return userGroupProcessor.create(userGroupCreateDtoRq)
}
```
- 2 Собственный spring-validator
``` kotlin
@Component
class UserGroupCreateDtoRqValidator : Validator {
    override fun supports(clazz: Class<*>): Boolean {
        return clazz.isAssignableFrom(UserGroupCreateDtoRq::class.java)
    }

    override fun validate(target: Any, errors: Errors) {
        target as UserGroupCreateDtoRq
        if (target.key == "test") {
            errors.rejectValue(
                "key",
                "NOT_ALLOWED_VALUE",
                "Недопустимое значение"
            )
        }
    }
}

@RestController
@RequestMapping("/admin/v1/user-groups", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "Группы пользователей", description = "Api настроек группы пользователей")
@Validated
class AdminUserGroupController
```
- 3 Валидация Query параметров
``` kotlin
@GetMapping
fun findAll(
    @Parameter(description = "Id сервиса с которыми связаны feature toggles", example = "foo-service", required = false)
    @RequestParam("serviceId", required = false)
    @Valid
    @NullOrNotBlank
    serviceId: String? = null,

    @Parameter(description = "Номер страницы результата, минимальное значение 0", example = "0", required = false)
    @RequestParam(name = "pageNumber", required = false)
    @Valid
    @Min(0, message = "Минимальное значение 0")
    pageNumber: Int? = null,

    @Parameter(description = "Размер страницы результата, минимальное значение 1, максимальное значение 100", example = "25", required = false)
    @RequestParam(name = "pageSize", required = false)
    @Valid
    @Min(1, message = "Минимальное значение 1")
    @Max(100, message = "Максимальное значение 100")
    pageSize: Int? = null
): PageDtoRs<FeatureToggleDetailDto> {
    return featureToggleProcessor.findAll(
        userGroupId = userGroupId,
        serviceId = serviceId,
        pageNumber = pageNumber ?: 0,
        pageSize = pageSize ?: 25
    )
}
```

- 3 Валидация двух LocalDateTime в query запроса
``` kotlin
@RestController
@RequestMapping("/v1/operations", produces = [MediaType.APPLICATION_JSON_VALUE])
@Tag(name = "История операций", description = "Api истории операций")
@Validated
class OperationHistoryController {

    @GetMapping
    fun getList(
        @Parameter(description = "От какой даты включительно требуются операции, формат ISO.DATE_TIME", example = "2023-08-02T00:00:00.000000", required = true)
        @RequestParam(name = "fromCreatedAt", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        fromCreatedAt: LocalDateTime,

        @Parameter(description = "До какой даты включительно требуются операции, формат ISO.DATE_TIME", example = "2030-12-31T23:59:59.999999", required = true)
        @RequestParam(name = "toCreatedAt", required = true)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        toCreatedAt: LocalDateTime,

        @Parameter(required = false, hidden = true)
        @Valid
        dateTimeRangeCriteria: CreatedAtDateTimeRangeCriteria // Если названия полей/текстовок/кода ошибки отличаются от примера реализуйте свой тип наследуя DateTimeRangeCriteria
    ) { ... }
}
```