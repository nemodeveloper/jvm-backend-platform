package ru.nemodev.platform.core.db.callback

import org.springframework.data.relational.core.mapping.event.BeforeConvertCallback
import ru.nemodev.platform.core.db.config.DataBaseProperties
import ru.nemodev.platform.core.db.entity.AbstractEntity
import ru.nemodev.platform.core.extensions.getGenericParameterClass
import ru.nemodev.platform.core.extensions.getSupperClassPrivateField
import ru.nemodev.platform.core.extensions.setSuperClassPrivateField
import ru.nemodev.platform.core.logging.sl4j.Loggable
import ru.nemodev.platform.core.service.generator.IdGeneratorService
import java.time.LocalDateTime
import java.util.*

/**
 * Колбек до конвертации сущности в объект БД
 * 1 - Работает только с AbstractEntity с 1 уровнем наследования
 * 2 - Генерирует id при вставке
 * 3 - Обновляет поля при update: updatedAt
 */
class UpdateBeforeConvertCallback(
    private val coreDbProperties: DataBaseProperties,
    private val idGeneratorService: IdGeneratorService
) : BeforeConvertCallback<Any> {

    companion object : Loggable {
        private const val ID_FIELD_NAME = "id"
    }

    override fun onBeforeConvert(aggregate: Any): Any {
        if (aggregate !is AbstractEntity<*>) {
            logWarn { "Сущность ${aggregate.javaClass.name} не наследуется от ${AbstractEntity::class.simpleName}, обновление полей до update в БД не поддерживается" }
            return aggregate
        }

        if (aggregate.getSupperClassPrivateField<Any>(ID_FIELD_NAME) == null) {
            when (val idClass = aggregate.getGenericParameterClass(0)) {
                String::class.java -> aggregate.setSuperClassPrivateField(ID_FIELD_NAME, idGeneratorService.generateId())
                UUID::class.java -> aggregate.setSuperClassPrivateField(ID_FIELD_NAME, idGeneratorService.generateUUID())
                else -> {
                    if (coreDbProperties.throwExceptionIfIdNull) {
                        throw IllegalStateException("Для сущности ${aggregate.javaClass.name} указан не поддерживаемый class $idClass для генерации id, поддерживаемые типы [String, UUID]")
                    }
                }
            }
        }

        if (!aggregate.isNew) {
            aggregate.updatedAt = LocalDateTime.now()
        }

        return aggregate
    }
}