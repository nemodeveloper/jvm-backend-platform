# Библиотека предоставляет возможности
- 1 Асинхронная обработка/выполнение задач

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-async")

## Changelogs

## 1 - Асинхронная обработка
Для того чтобы упростить обработку корутин в фоне в spring context есть 3 beans
- 1 ioCoroutineExecutor: CoroutineExecutor - в 99% случаев используйте именно его
- 2 defaultCoroutineExecutor: CoroutineExecutor
- 3 unconfinedCoroutineExecutor: CoroutineExecutor

Пример использования 

    ioCoroutineExecutor.async { println("Hello from async") }
    ioCoroutineExecutor.launch { println("Hello from launch") }

Или с помощью CoroutineExtensions

    doAsync { println("Hello from async") }
    doLaunch { println("Hello from launch") }
