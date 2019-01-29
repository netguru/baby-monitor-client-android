package co.netguru.baby.monitor.client.common

import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

object DateProvider {
    val midnight: LocalDateTime
        get() = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0)
    val yesterdaysMidnight: LocalDateTime
        get() = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).minusDays(1)

    val headerFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val timeStampFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
}
