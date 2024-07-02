package ru.nemodev.platform.core.service.generator

import com.fasterxml.uuid.Generators
import java.util.*

interface IdGeneratorService {
    fun generateUUID(): UUID
    fun generateId(): String
}

class IdGeneratorServiceImpl : IdGeneratorService {

    private val generator = Generators.timeBasedEpochGenerator()

    override fun generateUUID() = generator.generate()!!
    override fun generateId() = generateUUID().toString()
}