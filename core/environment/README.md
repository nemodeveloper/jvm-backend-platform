# Библиотека предоставляет информацию по окружению запущенного сервиса

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-environment")

## Changelogs


## Использование
- 1 Класс EnvironmentService позволяет получать текущий контур и env переменные, для этого заинжектите bean EnvironmentService
``` kotlin
private val environmentService: EnvironmentService
```
- 1.1 Для определения контура где запущен сервис необходимо задать env ENVIRONMENT значения в ContourType
