package co.netguru.baby.monitor.client.feature.debug

data class DebugState(
    val notificationInformation: String,
    val cryingProbability: Float,
    val decibels: Int
)
