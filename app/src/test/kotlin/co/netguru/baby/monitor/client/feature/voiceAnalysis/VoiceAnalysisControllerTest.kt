package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.common.NotificationHandler
import co.netguru.baby.monitor.client.common.base.ServiceController
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.ClientEntity
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.UserProperty
import co.netguru.baby.monitor.client.feature.babynotification.NotifyBabyEventUseCase
import co.netguru.baby.monitor.client.feature.debug.DebugModule
import co.netguru.baby.monitor.client.feature.feedback.FeedbackController
import co.netguru.baby.monitor.client.feature.feedback.SavedRecordingDetails
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import co.netguru.baby.monitor.client.feature.recording.RecordingController
import co.netguru.baby.monitor.client.feature.recording.RecordingData
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test

class VoiceAnalysisControllerTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val babyEvents = mock<Completable> {
        on { subscribe() } doReturn mock()
    }
    private val notifyBabyEventUseCase = mock<NotifyBabyEventUseCase> {
        on { babyEvents() } doReturn babyEvents
    }
    private val notificationHandler = mock<NotificationHandler>()
    private val debugModule = mock<DebugModule>()
    private val noiseDetector = mock<NoiseDetector>()
    private val dataRepository = mock<DataRepository>()
    private val machineLearning = mock<MachineLearning>()
    private val analyticsManager = mock<AnalyticsManager>()
    private val recordingDataSubjectMock = PublishSubject.create<RecordingData>()
    private val recordingController = mock<RecordingController>
    { on { startRecording() } doReturn recordingDataSubjectMock }
    private val recordingRawData = ByteArray(1)
    private val noiseRecordingData = RecordingData.NoiseDetection(recordingRawData, ShortArray(1))
    private val machineLearningRecordingData =
        RecordingData.MachineLearning(recordingRawData, ShortArray(1))
    private val voiceAnalysisService = mock<VoiceAnalysisService>()
    private val feedbackController = mock<FeedbackController> {
        on { handleRecording(any(), any()) } doReturn Single.just(SavedRecordingDetails("", false))
    }

    private val voiceAnalysisController = VoiceAnalysisController(
        notifyBabyEventUseCase,
        feedbackController,
        notificationHandler,
        debugModule,
        noiseDetector,
        dataRepository,
        machineLearning,
        analyticsManager,
        recordingController
    )

    @Test
    fun `should subscribe to baby events after recording start`() {
        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(RecordingData.Raw(ByteArray(1)))

        verify(notifyBabyEventUseCase).babyEvents()
        verify(babyEvents).subscribe()
    }

    @Test
    fun `should process noise detector data`() {
        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(noiseRecordingData)

        verify(noiseDetector).processData(noiseRecordingData.shortArray)
    }

    @Test
    fun `should process machine learning data`() {
        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(machineLearningRecordingData)

        verify(machineLearning).processData(machineLearningRecordingData.shortArray)
    }

    @Test
    fun `should handle processed noise detector data above the default threshold`() {
        val decibelsAboveThreshold = NoiseDetector.DEFAULT_NOISE_LEVEL + 10
        whenever(noiseDetector.processData(noiseRecordingData.shortArray)) doReturn Single.just<Int>(
            decibelsAboveThreshold
        )

        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(noiseRecordingData)

        verify(debugModule).sendSoundEvent(decibelsAboveThreshold)
        verify(feedbackController).handleRecording(recordingRawData, false)
        verify(notifyBabyEventUseCase).notifyNoiseDetected(any())
    }

    @Test
    fun `should handle processed noise detector data below the default threshold`() {
        val decibelsBelowThreshold = NoiseDetector.DEFAULT_NOISE_LEVEL - 10
        whenever(noiseDetector.processData(noiseRecordingData.shortArray)) doReturn Single.just<Int>(
            decibelsBelowThreshold
        )

        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(noiseRecordingData)

        verify(debugModule).sendSoundEvent(decibelsBelowThreshold)
        verify(notifyBabyEventUseCase, times(0)).notifyNoiseDetected()
        verify(feedbackController, times(0)).handleRecording(any(), any())
    }

    @Test
    fun `should handle processed machine learning data above the default threshold`() {
        val cryingProbabilityAboveThreshold = (MachineLearning.CRYING_THRESHOLD + 10).toFloat()
        val machineLearningData =
            mutableMapOf(MachineLearning.OUTPUT_2_CRYING_BABY to cryingProbabilityAboveThreshold)
        whenever(machineLearning.processData(machineLearningRecordingData.shortArray)) doReturn
                Single.just<MutableMap<String, Float>>(
                    machineLearningData
                )

        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(machineLearningRecordingData)

        verify(debugModule).sendCryingProbabilityEvent(cryingProbabilityAboveThreshold)
        verify(feedbackController).handleRecording(recordingRawData, true)
        verify(notifyBabyEventUseCase).notifyBabyCrying(any())
    }

    @Test
    fun `should handle processed machine learning data below the default threshold`() {
        val cryingProbabilityBelowThreshold = (MachineLearning.CRYING_THRESHOLD - 10).toFloat()
        val machineLearningData =
            mutableMapOf(MachineLearning.OUTPUT_2_CRYING_BABY to cryingProbabilityBelowThreshold)
        whenever(machineLearning.processData(machineLearningRecordingData.shortArray)) doReturn
                Single.just<MutableMap<String, Float>>(
                    machineLearningData
                )

        voiceAnalysisController.startRecording()
        recordingDataSubjectMock.onNext(machineLearningRecordingData)

        verify(debugModule).sendCryingProbabilityEvent(cryingProbabilityBelowThreshold)
        verify(feedbackController, times(0)).handleRecording(any(), any())
        verify(notifyBabyEventUseCase, times(0)).notifyBabyCrying()
    }

    @Test
    fun `should show foreground notification on attach service`() {
        whenever(dataRepository.getClientData()) doReturn Maybe.just(
            ClientEntity(
                "address",
                "key",
                VoiceAnalysisOption.NOISE_DETECTION
            )
        )

        (voiceAnalysisController as ServiceController<VoiceAnalysisService>).attachService(
            voiceAnalysisService
        )

        verify(notificationHandler).showForegroundNotification(voiceAnalysisService)
    }

    @Test
    fun `should set current voice analysis options on attach service`() {
        val voiceAnalysisOption = VoiceAnalysisOption.NOISE_DETECTION
        val noiseLevel = 50
        whenever(dataRepository.getClientData()) doReturn Maybe.just(
            ClientEntity("address", "key", voiceAnalysisOption, noiseLevel)
        )

        (voiceAnalysisController as ServiceController<VoiceAnalysisService>).attachService(
            voiceAnalysisService
        )

        assert(voiceAnalysisController.noiseLevel == noiseLevel)
        assert(voiceAnalysisController.voiceAnalysisOption == voiceAnalysisOption)
        verify(analyticsManager).setUserProperty(argThat {
            this is UserProperty.VoiceAnalysis && this.value == voiceAnalysisOption.name.toLowerCase()
        })
    }
}
