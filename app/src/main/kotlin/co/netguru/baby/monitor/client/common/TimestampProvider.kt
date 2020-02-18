package co.netguru.baby.monitor.client.common

import javax.inject.Inject

class TimestampProvider @Inject constructor() {
    fun timestamp() = System.currentTimeMillis()
}
