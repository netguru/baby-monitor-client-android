package co.netguru.baby.monitor.client.feature.feedback

import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.recording.RecordingFileController
import io.reactivex.Single
import javax.inject.Inject

class FeedbackController @Inject constructor(
    private val feedbackFrequencyUseCase: FeedbackFrequencyUseCase,
    private val firebaseSharedPreferencesWrapper: FirebaseSharedPreferencesWrapper,
    private val recordingFileController: RecordingFileController
) {
    fun handleRecording(
        recordingData: ByteArray,
        fromMachineLearning: Boolean
    ): Single<SavedRecordingDetails> {
        return if (userEnabledRecordingUpload()) {
            when {
                feedbackFrequencyUseCase.shouldAskForFeedback() -> {
                    recordingFileController.saveRecording(recordingData)
                        .map { SavedRecordingDetails(it, true) }
                }
                fromMachineLearning -> {
                    recordingFileController.saveRecording(recordingData)
                        .map { SavedRecordingDetails(it) }
                }
                else -> {
                    Single.just(SavedRecordingDetails(RECORDING_NOT_SAVED))
                }
            }
        } else {
            Single.just(SavedRecordingDetails(RECORDING_NOT_SAVED))
        }
    }

    private fun userEnabledRecordingUpload() = firebaseSharedPreferencesWrapper.isUploadEnabled()

    companion object {
        const val DATA_SIZE = MachineLearning.DATA_SIZE
        const val RECORDING_NOT_SAVED = ""
    }
}
