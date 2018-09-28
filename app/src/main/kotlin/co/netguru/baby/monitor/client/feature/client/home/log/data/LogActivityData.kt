package co.netguru.baby.monitor.client.feature.client.home.log.data

import org.threeten.bp.LocalDateTime

sealed class LogActivityData {

    abstract val timeStamp: LocalDateTime

    data class LogData(
            val action: String,
            override val timeStamp: LocalDateTime
    ) : LogActivityData()

    data class LogHeader(
            override val timeStamp: LocalDateTime
    ) : LogActivityData()

    companion object {
        //TODO remove when real data is provided
        fun getSampleData(): List<LogActivityData.LogData> {
            return mutableListOf<LogActivityData.LogData>().apply {
                for (i in 0L..80) {
                    add(LogData("Sample action $i", LocalDateTime.now().plusHours(i)))
                }
            }
        }
    }
}
