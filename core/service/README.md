# Библиотека предоставляет общие сервисы в виде java/kotlin api
- 1 Сервис генерации id/uuid

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-service")

## Changelogs

## Использование
### 1 - Bean idGeneratorService: IdGeneratorService 
Позволяет генерировать id/uuidv7
``` kotlin
idGeneratorService.generateId()
idGeneratorService.generateUUID()
```
