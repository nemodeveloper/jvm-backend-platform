# Библиотека предоставляет
- 1.1 store json удобная работа с JSONB полями в сущностях с помощью аннотаций
- 1.2 entity базовая логика для всех Entity
- 1.5 callback автоматические обновление базовых полей сущности AbstractEntity, например поля updatedAt

## Подключение библиотеки gradle
    implementation("ru.nemodev.platform:core-db")

ОЧЕНЬ ВАЖНО УБРАТЬ У СЕБЯ ЗАВИСИМОСТЬ DEVTOOLS, иначе магия не случится и не будет работать функционал store json из-за ClassLoader devtools

    developmentOnly(group = "org.springframework.boot", name = "spring-boot-devtools")

## Changelogs

## 1 - application.yml
``` yaml
spring:
  datasource:
     # Специфичные настройки каждой БД
     url: jdbc:postgresql://${DATABASE_URL:localhost:5432}/${DATABASE_NAME:service-db}
     username: ${DATABASE_USERNAME:service_admin}
     password: ${DATABASE_PASSWORD:service_admin}
     # Настройки ниже идут по умолчанию
    hikari:
      pool-name: ${spring.application.name:backend-app}
      maximum-pool-size: 4
      transaction-isolation: 4 # TRANSACTION_REPEATABLE_READ
      data-source-properties:
        ApplicationName: ${spring.application.name:backend-app}
  flyway:
    enabled: true
    locations: "classpath:flyway/migrations"
    baseline-on-migrate: true
    baseline-version: 0

platform:
  core:
    db:
      store-json-base-package: "ru.nemodev" # базовый пакет для поиска *Entity с аннотацией StoreJson
      throw-exception-if-id-null: true   # нужно ли бросать исключение если при вставке не был задан id и его не удалось сгенерировать
```

## 1.1 - Использование StoreJson
Функционал Store Json позволяет пометить классы аттрибутов сущности, чтобы они сохранялись в Postgres виде Json.

``` kotlin
@Table("payments")
class PaymentEntity(
  id: UUID? = null,   # если тип id будет UUID/String перед вставкой он будет автоматически сгенерирован, смотри UpdateBeforeConvertCallback
  createdAt: LocalDateTime = LocalDateTime.now(), # дата создания задается 1 раз при первом insert и больше не меняется
  updatedAt: LocalDateTime = createdAt, # дата обновления, обновляется автоматически перед update, смотри UpdateBeforeConvertCallback
  
  @Column("payment_detail")
  val paymentDetail: PaymentDetail
) : AbstractEntity<UUID>(id, createdAt, updatedAt)

@StoreJson 
data class PaymentDetail(
  val clientId: UUID,
  val productId: UUID,
  val summa: Money
  .. etc
)
```

## 1.2 - базовая логика для всех Entity
- 1 При написании своей Entity сущности необходимо наследоваться от AbstractEntity<>, которая содержит базовые поля
- 1.1 id - primary key, генерируется автоматически в случае если тип id UUID или String
- 1.2 createAd - дата-время создания сущности, проставляется автоматически
- 1.3 updatedAt - дата-время обновления сущности, при создании/обновлении проставляется автоматически
- 2 Все остальные поля стараемся сохранять в виде JSONB поля, так изменение сущности не приведет к исполнению DDL команд и связанных с ними проблем, например блокировок
- 2.1 Кроме того структура Json позволяет гибко добавлять новые поля в отличии от стандартной колоночной структуры
- 2.2 Индексы по JSONB полю работают также прекрасно как и по колоночным полям, https://postgrespro.ru/docs/postgrespro/9.5/datatype-json
- 2.3 Не создавайте без надобности GIN индекс, в большинстве случаев подойдет стандартный BTREE или HASH индекс

## 1.3 - Работа с транзакциями
Когда нужно явно управлять транзакцией?
- В случаях если вы сохраняете например N записей в БД, или делаете несколько обновлений разных сущностей
- Если вам нужно сохранить 1 сущность 1 раз, то использовать менеджер транзакций не нужно
- Запрещено использовать аннотацию Transactional во всех случаях без исключений 
- Запрещено в блоке транзакции делать какие-либо тяжелые действия(например HTTP вызовы, потенциально долгие вычисления).
Должны быть только вызовы методов Repository слоя

Для работы с транзакциями используем bean transactionTemplate