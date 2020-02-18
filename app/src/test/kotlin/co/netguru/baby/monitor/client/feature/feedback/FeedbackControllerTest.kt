package co.netguru.baby.monitor.client.feature.feedback

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.application.firebase.FirebaseSharedPreferencesWrapper
import co.netguru.baby.monitor.client.feature.recording.RecordingFileController
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.junit.Rule
import org.junit.Test

class FeedbackControllerTest {
    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val feedbackFrequencyUseCase = mock<FeedbackFrequencyUseCase>()
    private val firebaseSharedPreferencesWrapper = mock<FirebaseSharedPreferencesWrapper>()
    private val recordingData = ByteArray(1)
    private val recordingName = "recordingName"
    private val recordingFileController = mock<RecordingFileController> {
        on { saveRecording(recordingData) } doReturn Single.just(recordingName)
    }
    private val feedbackController = FeedbackController(
        feedbackFrequencyUseCase,
        firebaseSharedPreferencesWrapper,
        recordingFileController
    )

    @Test
    fun `should save file with feedback request`() {
        whenever(firebaseSharedPreferencesWrapper.isUploadEnabled()) doReturn true
        whenever(feedbackFrequencyUseCase.shouldAskForFeedback()) doReturn true

        feedbackController
            .handleRecording(recordingData, true)
            .test()
            .assertValue {
                it.shouldAskForFeedback && it.fileName == recordingName
            }
    }

    @Test
    fun `should save file without feedback request when it is from machine learning`() {
        whenever(firebaseSharedPreferencesWrapper.isUploadEnabled()) doReturn true
        whenever(feedbackFrequencyUseCase.shouldAskForFeedback()) doReturn false

        feedbackController
            .handleRecording(recordingData, true)
            .test()
            .assertValue {
                !it.shouldAskForFeedback && it.fileName == recordingName
            }
    }

    @Test
    fun `shouldn't save file without feedback request when it is from noise detection`() {
        whenever(firebaseSharedPreferencesWrapper.isUploadEnabled()) doReturn true
        whenever(feedbackFrequencyUseCase.shouldAskForFeedback()) doReturn false

        feedbackController
            .handleRecording(recordingData, false)
            .test()
            .assertValue {
                !it.shouldAskForFeedback && it.fileName == FeedbackController.RECORDING_NOT_SAVED
            }
    }

    @Test
    fun `shouldn't save file without user permission for upload`() {
        whenever(firebaseSharedPreferencesWrapper.isUploadEnabled()) doReturn false
        whenever(feedbackFrequencyUseCase.shouldAskForFeedback()) doReturn true

        feedbackController
            .handleRecording(recordingData, false)
            .test()
            .assertValue {
                !it.shouldAskForFeedback && it.fileName == FeedbackController.RECORDING_NOT_SAVED
            }
    }
}
