# Библиотека предоставляет возможности
- 1 Удобное расширение для Logger

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-logging-sl4j")

## Changelogs

## Использование
## 1.1 - Простое создание logger в ваших классах без LoggerFactory
Обычно для логирования мы пишем что-то такое
``` kotlin
class Foo {
    companion object {
        private val logger = LoggerFactory.getLogger(Foo::class.java)
    }
    fun bar() {
        logger.info("foo-bar")
    }
}
```

Интерфейс Loggable позволяет сделать следующее
``` kotlin
class Foo {
    companion object: Loggable
    fun bar() {
        logger.info("foo-bar")
    }
}
```

## 1.2 - Удобные расширение для logger
Продолжая пункт 1.1, написав так companion object: Loggable ваш класс получает набор функций из Loggable
``` kotlin
class Foo {
    companion object: Loggable
    fun bar() {
        logTrace("foo-bar")
        logDebug("foo-bar")
        logInfo("foo-bar")
        logWarn("foo-bar")
        logError("foo-bar")
    }
}
```
Кроме того есть расширения которые позволяют передать lambda в качестве строки для ленивого вычисления в процесс логирования, это полезно когда logger может быть недоступен, в таком случае строка не будет создана
Логируйте сообщения именно так
``` kotlin
class Foo {
    companion object: Loggable
    fun bar() {
        val count = 1
        logTrace { "count=$count" } 
        logger.trace { "count=$count" }
    }
}
```