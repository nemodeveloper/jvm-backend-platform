# Библиотека предоставляет набор вспомогательных классов для работы spring-* компонентов

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-spring")

## Changelogs

## Использование
- 1 Класс YamlPropertySourceFactory позволяет springboot starter задавать настройки по умолчанию с помощью *.yml файлов, таким образом после подключения springboot-starter нет необходимости задавать обязательные настройки если вас устраивают настройки по умолчанию
``` kotlin
@AutoConfiguration
@PropertySource(value = ["classpath:foo.yml"], factory = YamlPropertySourceFactory::class)
class FooConfig
```
- 1.1 Файл foo.yml размещаем в resources/* имя файла указываете по названию своей библиотеки