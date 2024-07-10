# Библиотека предоставляет возможности
- 1 Создание подключений к Kafka для отправки/получения сообщений
- 2 Bean Conditions для включения отключения producer/consumer
- 3 Метрики producer/consumer
- 4 Логирование сообщений producer/consumer
- 5 Создание описаний в формате AsyncApi

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-integration-kafka")

## Changelogs

## Использование
- 1 В контекст spring добавляется bean platformKafkaFactory: PlatformKafkaFactory, фабрика позволяет получать сконфигурированные producer/consumer kafka beans

## Конфигурация application.yml
Большинство настроек producer/consumer задаются автоматически в KafkaIntegrationProperties
Для producer требуется указать bootstrap-servers / topic
Для consumer требуется указать bootstrap-servers / topic / group-id / enable-auto-commit: false
Ниже приведет полный список настроек и их значения по умолчанию

``` yaml
wet-clinic:
  integration: // пример настройки kafka
    kafka: # Настройки KafkaIntegrationProperties
      producers:
        "[producer-test]":
          enabled: true # используется в паре с ConditionalOnKafkaProducerConfiguredCondition
          topic: "producer-test"
          logging-enabled: true       # Логирование
          logging-pretty-enabled: true # pretty json при логировании
          metrics-enabled: true       # Метрики
          tracing-enabled: true       # Трассировка
      consumers:
        "[consumer-test]":
          enabled: true # используется в паре с ConditionalOnKafkaConsumerConfiguredCondition
          topic: "consumer-test"
          concurrency: 1 # число kafka consumer в рамках одной единицы deploy, например если вы хотите чтобы pod обрабатывал n партиций вместо одной параллельно, формула расчета count = число партиций в топике / число подов
          logging-enabled: true     # Логирование
          logging-pretty-enabled: true # pretty json при логировании
          metrics-enabled: true     # Метрики
          tracing-enabled: true     # Трассировка
          kafka: # опциональная настройка для переопделения общих настроек из broker.consumer, необходимо когда у вас несколько consumer в сервисе, формат такой же KafkaProperties.Consumer
      broker: # Список самых важных базовых настроек Kafka на основе Spring KafkaProperties
        bootstrap-servers: localhost:9092
        producer:
          acks: all # если вам не страшно потерять сообщения укажите 0 или 1 для подтверждения приема только лидером, all все участники кластера приняли сообщения полная гарантия что сообщение не пропадет
          retries: 3 # повторные попытки отправки сообщения в kafka
          batch-size: 64KB # размер батча записей который накапливается перед отправкой в кафку, если у вас большой поток сообщений увеличьте буфер это позволит сократить число отправок в брокер
          buffer-memory: 32MB # максимальный размер буфера сообщений которые ожидают отправки в кафку
          compression-type: gzip # алгоритм сжатия сообщений перед отправкой в кафку, указывать настройку только если у вас интенсивная отправка больших сообщений
          properties: # настройки ниже не обязательны
            linger.ms: 0  # задержка перед тем как будет отправлена следующая пачка сообщений в кафку, значение > 0 полезно для накопления batch-size, если у вас интенсивная отправка указывайте 0
            max.request.size: 1048576 # 1MB максимальный размер пачки сообщений в байтах из накопившегося буфера при отправке в кафку
            max.in.flight.requests.per.connection: 5 # максимальное число запросов передаваемое за раз в рамках 1 коннекта к брокеру кафка, при ошибках и retry потенциально может изменится порядок отправляемых сообщений
        consumer:
          group-id: ${spring.application.name}
          enable-auto-commit: false # при true коммит происходит периодически не учитывая обработались сообщения или нет, при false коммит происходит автоматически батчингом при наполнении пачки обработанных сообщений
          properties: # список важных настроек по умолчанию
            auto.commit.interval.ms: 1000 # интервал автокоммита сообщений
            auto.offset.reset: earliest # алгоритм выборки записей если для consumer-group не задан offset latest/earliest
            partition.assignment.strategy: "org.apache.kafka.clients.consumer.CooperativeStickyAssignor" # алгоритм балансировки топиков/партиций для consumer-group
            session.timeout.ms: 10000 # время в течении которого считается что consumer жив если не поступали heartbeat.interval.ms, если это время выйдет начнется ребалансировка
            heartbeat.interval.ms: 3000 # интервал отправки координатору сообщения о том что consumer жив, должен быть не больше 1/3 session.timeout.ms
            max.poll.interval.ms: 300000 # максимальный интервал между запросами новых записей max.poll.records, если consumer за это время не успеет обработать сообщения начнется ребалансировка
            max.poll.records: 100 # количество записей выбираемое для обработки за раз, обработка записей происходит последовательно, например если max.poll.interval.ms 5 минут, а записей в пачке 100, то в среднем каждое сообщение должно успевать обрабатываться за 3 секунды
            fetch.max.bytes: 5242880 # 10MB максимальный объем данных получаемый при вычетке сообщений
        ssl: # Указываем если нужен ssl
          enabled: true 
          keystore-location: file:tls-keystore.jks
          truststore-location: file:tls-truststore.jks
          keystore-password: ${KEYSTORE_PASSWORD}
          truststore-password: ${TRUSTSTORE_PASSWORD}
          key-password: ${KEY_PASSWORD} 
          truststore-type: 'JKS'
          keystore-type: 'JKS'
          protocol: ssl
        properties: # указываем если нужна авторизация по логину/паролю
          sasl.mechanism: "SCRAM-SHA-512"
          sasl.jaas.config: org.apache.kafka.common.security.scram.ScramLoginModule required username=${KAFKA_USERNAME:admin} password=${KAFKA_PASSWORD:admin};
        security:
          protocol: "SSL" или "SASL_PLAINTEXT" в зависимости от настроек выше

logging:
  level:
    root: info # базовый уровень логирования МСа
    ru.nemodev.platform.core.integration.kafka.logging: trace # включение логирования KAFKA запросов, если указать другой уровень логи пропадут у всех consumer/producer
```

## Конфигурация spring
``` kotlin
@ConfigurationProperties("wet-clinic")
class WetClinicProperties(
    val integration: WetClinicIntegration
) {
    data class WetClinicIntegration(
        val kafka: KafkaIntegrationProperties
    )
}

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(WetClinicProperties::class)
class WetClinicIntegrationConfig {

    companion object {
        private const val WET_CLINIC_PRODUCER_KEY = "wet-clinic.pets.registrations"
        private const val WET_CLINIC_CONSUMER_KEY = "wet-clinic.pets.registrations"
    }

    @Bean
    fun wetClinicDefaultKafkaProducerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
    ): DefaultKafkaProducerFactory<String, PetRegistrationDtoRq> =
        platformKafkaFactory.createDefaultKafkaProducerFactory(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka
        )

    @Bean
    fun wetClinicKafkaTemplate(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
        wetClinicDefaultKafkaProducerFactory: DefaultKafkaProducerFactory<String, PetRegistrationDtoRq>
    ): KafkaTemplate<String, PetRegistrationDtoRq> =
        platformKafkaFactory.createKafkaTemplate(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka,
            wetClinicDefaultKafkaProducerFactory
        )

    @Bean
    fun wetClinicPlatformKafkaProducer(
        platformKafkaFactory: PlatformKafkaFactory,
        properties: WetClinicProperties,
        wetClinicKafkaTemplate: KafkaTemplate<String, PetRegistrationDtoRq>
    ): PlatformKafkaProducer<PetRegistrationDtoRq> =
        platformKafkaFactory.createProducer(
            WET_CLINIC_PRODUCER_KEY,
            properties.integration.kafka,
            wetClinicKafkaTemplate
        )

    @Bean
    fun wetClinicDefaultKafkaConsumerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory
    ): DefaultKafkaConsumerFactory<String, DeserializeResult<PetRegistrationDtoRq>> =
        platformKafkaFactory.createDefaultKafkaConsumerFactory(
            WET_CLINIC_CONSUMER_KEY,
            properties.integration.kafka,
            PetRegistrationDtoRq::class.java
        )

    @Bean
    fun wetClinicConcurrentKafkaListenerContainerFactory(
        properties: WetClinicProperties,
        platformKafkaFactory: PlatformKafkaFactory,
        wetClinicDefaultKafkaConsumerFactory: DefaultKafkaConsumerFactory<String, DeserializeResult<PetRegistrationDtoRq>>
    ): ConcurrentKafkaListenerContainerFactory<String, DeserializeResult<PetRegistrationDtoRq>> =
        platformKafkaFactory.createConcurrentKafkaListenerContainerFactory(
            WET_CLINIC_CONSUMER_KEY,
            properties.integration.kafka,
            wetClinicDefaultKafkaConsumerFactory
        )
}
```

## Отправка сообщений
``` kotlin
@AsyncGenericOperationBinding(
    type = "kafka",
    fields = [
        "bindingVersion=1.0.0",
        "groupId.type=string",
        "groupId.enum=wet-clinic.pets.registrations"
    ]
)
@AsyncPublisher(
    operation = AsyncOperation(
        channelName = "wet-clinic.pets.registrations.v1",
        description = "Публикация сообщения регистрации питомца",
        payloadType = PetRegistrationDtoRq::class
    )
)
private fun producePetRegistrationMessage(
    key: String,
    @Payload value: PetRegistrationDtoRq
) = wetClinicPlatformKafkaProducer.send(key, value)
```

## Получение сообщений
``` kotlin
@AsyncGenericOperationBinding(
    type = "kafka",
    fields = [
        "bindingVersion=1.0.0",
        "groupId.type=string",
        "groupId.enum=wet-clinic.pets.registrations"
    ]
)
@AsyncListener(
    operation = AsyncOperation(
        channelName = "wet-clinic.pets.registrations.v1",
        description = "Обработка полученного сообщения регистрации питомца",
        payloadType = PetRegistrationDtoRq::class
    )
)
@KafkaListener(
    containerFactory = "wetClinicConcurrentKafkaListenerContainerFactory"
)
fun process(@Payload message: ConsumerRecord<String, DeserializeResult<PetRegistrationDtoRq>>) {
    when(val petData = message.value()) {
        is DeserializeResult.Success -> {
            val petRegistration = petData.data

            val pet = petService.findById(petRegistration.id)
            if (pet != null) {
                pet.petDetail.wetClinicRegistered = true
                petService.update(pet)
                logInfo {
                    "Питомец id = ${pet.id} name = ${pet.petDetail.name} поставлен на учет в вет клинике =)"
                }
            }
        }
        is DeserializeResult.Failed -> {

        }
    }
}
```

## Описание AsyncApi
Библиотека позволяет создавать описания в формате AsyncApi при помощи аннотаций Springwolf и Swagger.
Описание доступно через UI по адресу {base-url}/{service-name}/springwolf/asyncapi-ui.html

### Пример описания producer:
```kotlin
class WetClinicKafkaMessageProducer {

    @AsyncGenericOperationBinding(
        type = "kafka",
        fields = [
            "bindingVersion=1.0.0",
            "groupId.type=string",
            "groupId.enum=wet-clinic.pets.registrations"
        ]
    )
    @AsyncPublisher(
        operation = AsyncOperation(
            channelName = "wet-clinic.pets.registrations.v1",
            description = "Публикация сообщения регистрации питомца",
            payloadType = Message::class
        )
    )
    fun publish(@Payload message: MyMessageEventDto) {
    }
}
```

### Пример описания consumer:
```kotlin
class WetClinicKafkaMessageProcessor : KafkaMessageProcessor<PetRegistrationDtoRq> {

    @AsyncGenericOperationBinding(
        type = "kafka",
        fields = [
            "bindingVersion=1.0.0",
            "groupId.type=string",
            "groupId.enum=wet-clinic.pets.registrations"
        ]
    )
    @AsyncListener(
        operation = AsyncOperation(
            channelName = "wet-clinic.pets.registrations.v1",
            description = "Обработка полученного сообщения регистрации питомца",
            payloadType = PetRegistrationEventDto::class
        )
    )
    fun process(@Payload message: ConsumerRecord<String, DeserializeResult<PetRegistrationDtoRq>>) {
    }
}
```

### Пример конфигурации springwolf и их значения по умолчанию
```yaml
springwolf:
  enabled: true
  docket:
    info:
      title: ${spring.application.name}
      version: 1.0.0
      contact:
        name: ${springdoc.open-api.info.contact.name}
        email: ${springdoc.open-api.info.contact.email}
        url: ${springdoc.open-api.info.contact.url}
    base-package: ru.nemodev
    servers:
      local:
        protocol: kafka
        description: local
        host: localhost:9092
```
