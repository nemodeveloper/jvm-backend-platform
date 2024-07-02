package ru.nemodev.platform.core.db.callback

import org.springframework.data.relational.core.mapping.event.AfterSaveCallback
import ru.nemodev.platform.core.db.entity.AbstractEntity
import ru.nemodev.platform.core.logging.sl4j.Loggable

/**
 * Колбек обновления сущности после вставки в БД
 * 1 - Работает только с AbstractEntity
 * 2 - Устанавливает isNew = false для последующих обновлений в БД
 * Например сохранили новую сущность и после нужно обновить поле и снова сохранить сущность
 */
class UpdateAfterSaveCallback : AfterSaveCallback<Any> {

    companion object : Loggable

    override fun onAfterSave(aggregate: Any): Any {
        if (aggregate !is AbstractEntity<*>) {
            logWarn { "Сущность ${aggregate.javaClass.simpleName} не наследуется от ${AbstractEntity::class.simpleName}, обновление поля isNew после insert не поддерживается" }
            return aggregate
        }

        aggregate.isNew = false

        return aggregate
    }

}