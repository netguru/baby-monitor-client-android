package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.common.RunsInBackground
import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import io.reactivex.Observable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class RecordingController @Inject constructor(
    private val aacRecorder: AacRecorder
) {
    var voiceAnalysisOption: VoiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING
    private var rawData = emptyArray<Byte>()
    private var newData = emptyArray<Short>()

    fun startRecording(): Observable<RecordingData> {
        return aacRecorder.startRecording()
            .doOnNext {
                addData(it)
            }
            .map {
                val data = when {
                    voiceAnalysisOption == VoiceAnalysisOption.MACHINE_LEARNING &&
                            newData.size >= MachineLearning.DATA_SIZE -> RecordingData.MachineLearning(
                        rawData.toByteArray(),
                        newData.toShortArray()
                    )
                    voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION &&
                            newData.size >= NoiseDetector.DATA_SIZE -> RecordingData.NoiseDetection(
                        newData.takeLast(NoiseDetector.DATA_SIZE).toShortArray()
                    )
                    else -> RecordingData.Raw(it)
                }
                if (newData.size >= MachineLearning.DATA_SIZE) clearRecordingData()
                data
            }
    }

    @RunsInBackground
    fun addData(array: ByteArray) {
        newData = newData.plus(bytesToShorts(array).toTypedArray())
        rawData = rawData.plus(array.toTypedArray())
    }

    @RunsInBackground
    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    private fun clearRecordingData() {
        newData = emptyArray()
        rawData = emptyArray()
    }
}
