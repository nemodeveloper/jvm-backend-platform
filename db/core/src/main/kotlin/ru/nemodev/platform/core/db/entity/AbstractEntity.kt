package ru.nemodev.platform.core.db.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.InsertOnlyProperty
import java.time.LocalDateTime

/**
 * Базовая сущность для всех объектов которые хранятся в БД
 */
abstract class AbstractEntity<ID_TYPE>(

    @Id
    @Column("id")
    private var id: ID_TYPE? = null,

    @Column("created_at")
    @InsertOnlyProperty
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column("updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : Persistable<ID_TYPE> {

    @Transient
    private var justCreated = id == null

    override fun isNew() = justCreated
    fun setNew(new: Boolean) { justCreated = new }

    override fun getId() = id!!
}