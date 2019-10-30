package co.netguru.baby.monitor.client.common

import org.threeten.bp.LocalDateTime
import javax.inject.Inject

class LocalDateTimeProvider @Inject constructor() {
    fun now(): LocalDateTime = LocalDateTime.now()
}
