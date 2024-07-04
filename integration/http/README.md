# Библиотека предоставляет общий метод для интеграции с сервисами по http

Для ее использования необходимо в проект добавить зависимость

    implementation("ru.nemodev.platform:core-integration-http")

## Changelogs

## Использование
- 1 В контекст spring добавляется bean RestClientFactory, фабрика позволяет получать сам `RestClient` или `RestClientBuilder` 

## Конфигурация spring
```kotlin
@AutoConfiguration
class SomeRestClientConfiguration {
    
    @Bean
    fun someServiceNameRestClient(
      restClientFactory: RestClientFactory, 
      someServiceNameProperties: SomeServiceNameProperties
    ) = restClientFactory.create(someServiceNameProperties.integration.httpClient)
}

@ConfigurationProperties("app.some-service-name")
class SomeServiceNameProperties(
    val integration: SomeServiceNameIntegration
) {
    data class SomeServiceNameIntegration(
        val httpClient: HttpClientProperties
    )
}
```

Конфигурация на уровне spring bean
Для дополнительной конфигурации RestClient есть несколько вариантов
- 1 Реализовать RestClientPropertyCustomizer
- 2 Получать restClientFactory.createBuilder(someServiceNameProperties.integration.httpClient)
- 3 Изменение настроек по умолчанию через application.yml

## Конфигурация application.yml
```yml
app:
  some-service-name:
    integration:
      http-client:
        serviceId: test-service
        url: http://localhost:8083
        # настройки ниже задаются по умолчанию смотри HttpClientProperties
        timeout:
          connection: 3000ms
          read: 3000ms
        logging-enabled: true  # логирование запрос/ответа
        observation-enabled: true # prometheus метрики + tracing
        redirect-enabled: false # автоматический redirect на ресурс при ответе
        proxy: # настройка прокси-сервера, опциональная настройка
          host: http://localhost
          port: 8081
          username: username
          password: password
        ssl:  # настройки ssl, опциональная настройка
          keystore-location: file:/home/jboss/pki/tls-keystore.jks
          keystore-password: ${TLS_KEYSTORE_PASSWORD}
          key-password: ${TLS_KEY_PASSWORD}
          truststore-location: file:/home/jboss/pki/tls-truststore.jks
          truststore-password: ${TLS_TRUSTSTORE_PASSWORD}
        headers: # передача каких либо констант на уровне заголовков, опциональная настройка
          - name: foo
            value: bar
        retry:
          enabled: true # По-умолчанию false
          delay: 1s # Время задержки перед повторным запросом. По-умолчанию 1 секунда.
          max-attempts: 3 # Максимальное кол-во ретраев. По-умолчанию 3
          methods: GET # Тип метода запроса, по которым надо делать ретраии. По-умолчанию GET
          status-codes: 429,500,503 # Список кодов ошибок в случае которых необходимо выполнять retry

# Настройки логеров для логирования http rq/rs задаются по умолчанию
logging:
  level:
    org.zalando.logbook: trace
    ru.nemodev.platform.core.logging: trace
```


