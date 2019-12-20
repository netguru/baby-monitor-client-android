package co.netguru.baby.monitor.client.feature.settings

sealed class ResetState {
    object InProgress: ResetState()
    object Completed: ResetState()
    object Failed: ResetState()
}
