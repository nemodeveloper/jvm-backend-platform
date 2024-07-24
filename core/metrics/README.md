# Библиотека предоставляет возможности

- 1 Библиотека подключает actuator и экспортер в формате Prometheus базовых метрик Springboot
- 2 Настраивает общие метрики и actuator
- 3 Настраивает spring-boot-admin-client

## Подключение библиотеки gradle

    implementation("ru.nemodev.platform:core-metrics")

## Changelogs

## Просмотр метрик сервиса
- 1 - Перейти по url host:port/actuator/prometheus

## Создание своих собственных метрик для экспорта в prometheus
- 1 - Counter тип метрики показывающий накопительный итог во времени, например число вызовов метода 
``` kotlin
class AwesomeService(
    meterRegistry: MeterRegistry
) {
    private val petListCallCounter 
        = Counter
            .builder("pets.list")
            .description("Pets list method call counter")
            .register(meterRegistry)
    
    fun findAll(): List<Pet> {
        return repository.findAll().also {
            petListCallCounter.increment()
        }
    }
}
```

- 2 - Gauge тип метрики показывающий постоянное значение которое может меняться во времени, например может отображать статус работы шедулера или другую постоянную величину
``` kotlin
class AwesomeService(
    meterRegistry: MeterRegistry
) {
    private val lastAddedPetType = AtomicReference(PetTypeDto.UNKNOWN)
    private val lastAddedPetTypeGauge
        = Gauge
            .builder("pets.last_added_type") {
                lastAddedPetType.get().intType()
            }
            .description("Last added pet type")
            .register(meterRegistry)
    
    fun create(petDtoRq): Pet {
        val pet = ...
        return repository.save(pet).also {
            lastAddedPetType.set(it.type)
        }
    }
}
```

## Автоконфигурация
- 1 Библиотека автоматически конфигурирует выставление метода /actuator/prometheus , конфиг смотри в resources/core-metrics.yml