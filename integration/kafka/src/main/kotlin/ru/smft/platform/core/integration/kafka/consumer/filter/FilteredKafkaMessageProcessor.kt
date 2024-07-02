package ru.smft.platform.core.integration.kafka.consumer.filter

import reactor.kafka.receiver.ReceiverRecord
import ru.smft.platform.core.integration.kafka.consumer.KafkaMessageProcessor
import ru.smft.platform.core.integration.kafka.deserializer.DeserializeResult
import ru.smft.platform.core.integration.kafka.extension.toLog
import ru.smft.platform.core.logging.sl4j.Loggable

/**
 * Обработчик kafka сообщений который производит фильтрацию сообщений
 * 1 - В случае если сообщение пройдет все фильтры filters KafkaMessageFilter сообщение передается на обработку в delegate KafkaMessageProcessor
 * 2 - Иначе сообщение считается обработанным и в лог пишется сообщение о том что фильтрация не прошла под уровнем debug
 * 3 - Переопределив метод getEntityLog в логирование можно добавить важную информацию из сообщения
 */
open class FilteredKafkaMessageProcessor<T>(
    private val delegate: KafkaMessageProcessor<T>,
    private val filters: List<KafkaMessageFilter<T>>
) : KafkaMessageProcessor<T> {

    companion object : Loggable

    override suspend fun process(message: ReceiverRecord<String, DeserializeResult<T>>) {
        try {
            when (val messageValue = message.value()) {
                is DeserializeResult.Success<T> -> {
                    if (filters.all { it.filter(messageValue.data) }) {
                        delegate.process(message)
                    } else {
                        logDebug { "Сообщение ${getLogByMessage(message)} не будет обработано т.к не прошло фильтрацию" }
                    }
                }
                is DeserializeResult.Failed -> {
                    logError(messageValue.e) {
                        "Ошибка парсинга сообщения ${getLogByMessage(message)}"
                    }
                    delegate.process(message)
                }
            }
        }
        catch (e: Throwable) {
            logError(e) {
                "Произошла ошибка обработки сообщения ${getLogByMessage(message)}"
            }
            throw e
        }
    }

    protected open fun getLogByMessage(message: ReceiverRecord<String, DeserializeResult<T>>): String {
        val entityLog = getEntityLog(message)
        return if (entityLog.isEmpty()) getKafkaLog(message)
        else "$entityLog ${getKafkaLog(message)}"
    }

    protected open fun getEntityLog(message: ReceiverRecord<String, DeserializeResult<T>>): String {
        return ""
    }

    protected open fun getKafkaLog(message: ReceiverRecord<String, DeserializeResult<T>>) = message.toLog()
}