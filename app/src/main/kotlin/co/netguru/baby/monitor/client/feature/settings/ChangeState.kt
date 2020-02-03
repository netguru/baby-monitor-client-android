package co.netguru.baby.monitor.client.feature.settings

sealed class ChangeState {
    object InProgress : ChangeState()
    object Completed : ChangeState()
    object Failed : ChangeState()
}
