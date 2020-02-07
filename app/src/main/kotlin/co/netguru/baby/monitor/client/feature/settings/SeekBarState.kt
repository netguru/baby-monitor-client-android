package co.netguru.baby.monitor.client.feature.settings

sealed class SeekBarState {
    data class StartTracking(val initialValue: Int) : SeekBarState()
    data class ProgressChange(val progress: Int) : SeekBarState()
    data class EndTracking(val endValue: Int) : SeekBarState()
}
