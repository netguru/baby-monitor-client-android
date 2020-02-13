package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.RxSchedulersOverrideRule
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.reactivex.subjects.PublishSubject
import org.junit.Rule
import org.junit.Test

class RecordingControllerTest {

    @get:Rule
    val schedulersRule = RxSchedulersOverrideRule()

    private val startRecordingSubjectMock = PublishSubject.create<ByteArray>()
    private val recorder = mock<AacRecorder> {
        on { startRecording() } doReturn startRecordingSubjectMock
    }
    private val recordingController = RecordingController(recorder)
    private val rawByteArrayMachineLearningSize = MachineLearning.DATA_SIZE * 2
    private val rawByteArrayeNoiseDetectionSize = NoiseDetector.DATA_SIZE * 2
    private val testObserver = recordingController.startRecording().test()

    @Test
    fun `should return rawData if data size conditions for machineLearning aren't met`() {
        val rawData = ByteArray(rawByteArrayMachineLearningSize - 1)
        recordingController.voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING

        startRecordingSubjectMock.onNext(rawData)

        testObserver.assertValue { it is RecordingData.Raw }
    }

    @Test
    fun `should return rawData if data size conditions for noiseDetection aren't met`() {
        val rawData = ByteArray(rawByteArrayeNoiseDetectionSize - 1)
        recordingController.voiceAnalysisOption = VoiceAnalysisOption.NOISE_DETECTION

        startRecordingSubjectMock.onNext(rawData)

        testObserver.assertValue { it is RecordingData.Raw }
    }

    @Test
    fun `should return machineLearningData if data size conditions for machineLearning are met`() {
        val rawData = ByteArray(rawByteArrayMachineLearningSize)
        recordingController.voiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING

        startRecordingSubjectMock.onNext(rawData)

        testObserver.assertValue { it is RecordingData.MachineLearning }
    }

    @Test
    fun `should return noiseDetectionData if data size conditions for noiseDetection are met`() {
        val rawData = ByteArray(rawByteArrayeNoiseDetectionSize)
        recordingController.voiceAnalysisOption = VoiceAnalysisOption.NOISE_DETECTION

        startRecordingSubjectMock.onNext(rawData)

        testObserver.assertValue { it is RecordingData.NoiseDetection }
    }
}
