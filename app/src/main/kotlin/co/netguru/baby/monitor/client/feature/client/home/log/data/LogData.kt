package co.netguru.baby.monitor.client.feature.client.home.log.data

import org.threeten.bp.LocalDateTime

sealed class LogData {

    abstract val timeStamp: LocalDateTime

    data class Data(
            val action: String,
            override val timeStamp: LocalDateTime,
            val image: String? = null
    ) : LogData()

    data class LogHeader(
            override val timeStamp: LocalDateTime
    ) : LogData()
}
