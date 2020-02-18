package co.netguru.baby.monitor.client.feature.feedback

data class SavedRecordingDetails(
    val fileName: String,
    val shouldAskForFeedback: Boolean = false
)
