package ru.smft.platform.core.integration.kafka.serialaizer

import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.serialization.Serializer
import ru.smft.platform.core.logging.mdc.MdcService
import ru.smft.platform.core.logging.sl4j.Loggable

class MdcHeadersSerializer<T>(
    private val delegate: Serializer<T>,
    private val mdcService: MdcService
) : Serializer<T> by delegate {

    companion object : Loggable

    override fun serialize(topic: String, headers: Headers?, data: T?): ByteArray? {
        try {
            mdcService.fillRequestHeadersFromMdc { name, value ->
                // Добавляем MDC заголовки только если заголовка нет
                // Чтобы не перетирать пользовательские заголовки
                if (headers?.lastHeader(name) == null) {
                    headers?.add(name, value.toByteArray())
                }
            }
        } catch (e: Throwable) {
            logger.error("Ошибка заполнения kafka headers из mdc context, topic = $topic", e)
        }
        return delegate.serialize(topic, headers, data)
    }
}