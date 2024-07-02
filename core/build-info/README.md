# Библиотека предоставляет возможности
- 1 Информация по сборке сервиса на основе git в /actuator/info
- 2 Кастомизация spring banner с выводом ключевой информации на основе git

## Подключение библиотеки gradle
    
    implementation("ru.nemodev.platform:core-build-info")

## Changelogs

## Использование
- 1 Класс BuildInfoService позволяет получать информацию по сборке сервиса, для этого заинжектите bean BuildInfoService
``` kotlin
private val buildInfoService: BuildInfoService
```

- 2 Информация по сборке сервиса на основе git в /actuator/info
- 2.1 - Добавить в build.gradle.kts в секцию plugins id("com.gorylenko.gradle-git-properties") version "2.4.2"

- 3 Кастомизация spring banner с выводом ключевой информации на основе git
- Корректно работает только при выполнении пункта 2 
- 1 - По умолчанию реализован общий resources/banner.txt
- 2 - Можно расширить параметры git в Build Info секции баннера
- 2.1 - Для этого скопируйте banner.txt к себе в проект и укажите желаемые параметры, доступные параметры смотрите после gradle task build вашего сервиса в build/resources/git.properties
- 3 - Так же можно заменить баннер на название вашего сервиса
- 3.1 - Для этого используйте сервис https://devops.datenkollektiv.de/banner.txt/index.html font star-wars
- 4 - Ограничения по использования переменных в banner.txt
- 4.1 - Вывод баннера происходит до создания spring context поэтому использовать значения получится только из application.yml и env, для этого реализован ApplicationInfoAppRunListener
- 4.2 - Вывод переменных git реализован через GitBuildInfoAppRunListener