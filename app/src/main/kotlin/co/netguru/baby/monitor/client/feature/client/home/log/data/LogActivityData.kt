package co.netguru.baby.monitor.client.feature.client.home.log.data

import org.threeten.bp.LocalDate

sealed class LogActivityData {

    data class LogData(
            val action: String,
            val timeStamp: LocalDate
    ) : LogActivityData()

    data class LogHeader(
            val text: String
    ) : LogActivityData()

    companion object {
        //TODO remove when real data is provided
        fun getSampleData(): List<LogActivityData> {
            return mutableListOf<LogActivityData>().apply {
                add(LogHeader("Today"))
                for (i in 0..10) {
                    add(LogData("first $i", LocalDate.now()))
                }
                add(LogHeader("Yesterday"))
                for (i in 0..10) {
                    add(LogData("second $i", LocalDate.now()))
                }
                add(LogHeader("Third"))
                for (i in 0..10) {
                    add(LogData("third $i", LocalDate.now()))
                }

            }
        }
    }
}



