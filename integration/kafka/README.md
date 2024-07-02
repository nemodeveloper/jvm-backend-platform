# Библиотека предоставляет возможности
- 1 Создание подключений к Kafka для отправки/получения/логирования сообщений в едином стиле
- 2 Как правильно отправлять/получать сообщения через kafka
- 3 Bean Conditions для включения отключения producer/consumer
- 4 Метрики consumer
- 5 Фильтрация kafka consumer сообщений
- 6 Создание описаний в формате AsyncApi

## Подключение библиотеки gradle
    
    implementation("ru.smftp.core:core-integration-kafka")

## Changelogs
- 1.0.7 Обновлены версии библиотек spring-boot -> 3.2.0
- 1.2.10 Доработан механизм коммита сообщений
- 1.2.13 Добавлены зависимости springwolf для генерации описаний AsyncApi
- 1.2.14 Рефакторинг WebClientProperties и KafkaIntegrationProperties, частично починена подсветка в application.yml

## Конфигурация spring
``` kotlin
@ConfigurationProperties(prefix = "smft.integration")
data class SmftIntegrationKafkaProperties(
    val kafka: KafkaIntegrationProperties
)

@AutoConfiguration
@EnableConfigurationProperties(SmftIntegrationKafkaProperties::class)
class KafkaTestConfig {
    
    /**
    * После того как Consumer будет создан, после события ApplicationReady начнется обработка сообщений
    */
    @Bean
    fun testSmartKafkaConsumer(
        smartKafkaConsumerFactory: SmartKafkaConsumerFactory, 
        smftKafkaProperties: SmftKafkaProperties,
        testKafkaMessageProcessor: KafkaMessageProcessor<Foo>
    ): SmartKafkaConsumer<Foo> = smartKafkaConsumerFactory.create("consumer-key", smftKafkaProperties.kafka, testKafkaMessageProcessor, Foo::class.java)
    
    @Bean
    fun testSmartKafkaProducer(
        smartKafkaProducerFactory: SmartKafkaProducerFactory,
        smftKafkaProperties: SmftKafkaProperties
    ): SmartKafkaProducer<Foo> = smartKafkaProducerFactory.create("producer-key", smftKafkaProperties.kafka)
}

@Component
class TestKafkaMessageProcessor : KafkaMessageProcessor<Foo> {

    companion object : Loggable

    override suspend fun process(message: ReceiverRecord<String, DeserializeResult<Foo>>) {
        when(val fooValue = message.value()) {
            is DeserializeResult.Success -> {
                val foo = fooValue.data
                // ... do something
            }
            is DeserializeResult.Failed -> {
                // foo message parsing error
            }
        }
    }
}
```

## Отправка сообщений, полезная статья https://projectreactor.io/docs/kafka/release/reference/#api-guide-receiver
``` kotlin
testSmartKafkaProducer.send("message-key", message)
```

## Конфигурация application.yml

``` yaml
smftp:
  core:
    integration:  # по умолчанию установлены настройки ниже в большинстве случаев явно указывать их и менять не нужно
      kafka:
        metrics:
          consumer:
            enabled: true # включение или отключение метрик у всех consumer
            initial-refresh-delay: 180s # первоначальная задержка обновления kafka consumer metrics, не рекомендуется уменьшать это значение т.к может привести к ошибкам
            refresh-delay: 30s # период обновления kafka consumer metrics, не рекомендуется уменьшать данное значение чтобы не спамить kafka broker запросами
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
          fill-headers-from-mdc: true # Нужно ли заполнять заголовки значениями из mdc контекста
      consumers:
        "[consumer-test]":
          enabled: true # используется в паре с ConditionalOnKafkaConsumerConfiguredCondition
          topic: "consumer-test"
          count: 1 # число kafka consumer в рамках одной единицы deploy, например если вы хотите чтобы pod обрабатывал n партиций вместо одной параллельно, формула расчета count = число партиций в топике / число подов
          logging-enabled: true     # Логирование
          logging-pretty-enabled: true # pretty json при логировании
          metrics-enabled: true     # Метрики
          tracing-enabled: true     # Трассировка
          fill-mdc-from-headers: true # Нужно ли заполнять mdc контекст значениями из заголовков
          kafka-extended: # расширенные настройки kafka consumer которые нельзя задать через KafkaProperties.Consumer
            auto-commit-batch-size: 10 # размер пачки автокомита обработанных сообщений
            max-poll-timeout: 10000ms # timeout получения записей при чтении из kafka
          kafka: # опциональная настройка для переопделения общих настроек из broker.consumer, необходимо когда у вас несколько consumer в сервисе, формат такой же KafkaProperties.Consumer
          retry-process-delay: 3000ms # задержка повторной обработки сообщения при возникновении ошибки
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
          enable-auto-commit: false # при true коммит происходит периодически не учитывая обарботались сообщения или нет, при false коммит происходит автоматически батчингом при наполнении пачки обработанных сообщений
          properties: # список важных настроек по умолчанию
            auto.commit.interval.ms: 3000 # интервал автокоммита сообщений
            auto.offset.reset: earliest # алгоритм выборки записей если для consumer-group не задан offset latest/earliest
            partition.assignment.strategy: "org.apache.kafka.clients.consumer.CooperativeStickyAssignor" # алгоритм балансировки топиков/партиций для consumer-group
            session.timeout.ms: 10000 # время в течении которого считается что consumer жив если не поступали heartbeat.interval.ms, если это время выйдет начнется ребалансировка
            heartbeat.interval.ms: 3000 # интервал отправки координатору сообщения о том что consumer жив, должен быть не больше 1/3 session.timeout.ms
            max.poll.interval.ms: 300000 # максимальный интервал между запросами новых записей max.poll.records, если consumer за это время не успеет обработать сообщения начнется ребалансировка
            max.poll.records: 100 # количество записей выбираемое для обработки за раз, обработка записей происходит последовательно, например если max.poll.interval.ms 5 минут, а записей в пачке 100, то в среднем каждое сообщение должно успевать обрабатываться за 3 секунды
            fetch.max.bytes: 5242880 # 10MB максимальный объем данных получаемый при вычетке сообщений
        ssl: # Указываем если нужен ssl
          enabled: true 
          keystore-location: file:/home/jboss/pki/tls-keystore.jks
          truststore-location: file:/home/jboss/pki/tls-truststore.jks
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
    ru.smft.platform.core.integration.kafka.logging: trace # включение логирования KAFKA запросов, если указать другой уровень логи пропадут у всех consumer/producer
```

## Bean Conditions
Библиотека включает в себя кастомные Spring Conditions.
Если вам требуется создавать бины producer/consumer в зависимости от наличия их конфигурации в application.yaml - воспользуйтесь аннотациями *ConditionalOnKafkaConsumerConfigured* и *ConditionalOnKafkaProducerConfigured* 

В аннотациях указывается kafkaPrefix - путь до параметра конфигурации KafkaIntegrationProperties,
а также проверяемый consumerKey / producerKey, с настройкой enabled: true/false, при наличии которого в файле конфигурации создастся бин

``` kotlin
    @Bean
    @ConditionalOnKafkaConsumerConfigured(kafkaPrefix = "smft.integration.kafka", consumerKey = "consumer-test")
    fun testSmartConsumer() = ....
```

## Метрики
При включении настройки smft.core.integration.kafka.metrics.consumer.enabled = true в actuator/prometheus появятся метрики:
### Consumer
- kafka_consumer_lag
- kafka_consumer_current_offset
- kafka_consumer_end_offset

## Фильтрация kafka consumer сообщений KafkaMessageFilter FilteredKafkaMessageProcessor
Если у вас есть необходимость фильтровать получаемые сообщения используйте FilteredKafkaMessageProcessor + KafkaMessageFilter

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
    override suspend fun publish(@Payload message: Message) {
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
            payloadType = PetRegistrationDtoRq::class
        )
    )
    override suspend fun process(@Payload message: ReceiverRecord<String, DeserializeResult<PetRegistrationDtoRq>>) {
    }
}
```

### Пример конфигурации параметров (необязательно)
```yaml
springwolf:
  enabled: true
  docket:
    info:
      title: ${spring.application.name}
      version: 1.0.0
      contact:
        name: ${smftp.core.open-api.contact.name}
        email: ${smftp.core.open-api.contact.email}
        url: ${smftp.core.open-api.contact.url}
    base-package: ru.smft
    servers:
      stage-kafka-bus-0:
        protocol: kafka
        host: kafka-bus.samoletgroup.ru:30010
      stage-kafka-bus-1:
        protocol: kafka
        host: kafka-bus.samoletgroup.ru:30011
      stage-kafka-bus-2:
        protocol: kafka
        host: kafka-bus.samoletgroup.ru:30012
```
