package co.netguru.baby.monitor.client.feature.voiceAnalysis

import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.noisedetection.NoiseDetector
import io.reactivex.Observable
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class RecordingController @Inject constructor(
    private val aacRecorder: AacRecorder
) {
    @Volatile
    var voiceAnalysisOption: VoiceAnalysisOption = VoiceAnalysisOption.MACHINE_LEARNING

    fun startRecording(): Observable<RecordingData> {
        return aacRecorder.startRecording()
            .scan(newAccumulator(), { acc, new ->
                if (acc.second.size >= MachineLearning.DATA_SIZE) {
                    addData(newAccumulator(), new)
                } else {
                    addData(acc, new)
                }
            })
            .filter { it.first.isNotEmpty() && it.second.isNotEmpty() }
            .map {
                val data = when {
                    isMachineLearningWithEnoughData(it) -> RecordingData.MachineLearning(
                        it.first.toByteArray(),
                        it.second.toShortArray()
                    )
                    isNoiseDetectionWithEnoughData(it) -> RecordingData.NoiseDetection(
                        it.second.takeLast(NoiseDetector.DATA_SIZE).toShortArray()
                    )
                    else -> RecordingData.Raw(it.first.toByteArray())
                }
                data
            }
    }

    private fun isNoiseDetectionWithEnoughData(it: Pair<Array<Byte>, Array<Short>>) =
        voiceAnalysisOption == VoiceAnalysisOption.NOISE_DETECTION &&
                it.second.size >= NoiseDetector.DATA_SIZE

    private fun isMachineLearningWithEnoughData(it: Pair<Array<Byte>, Array<Short>>) =
        voiceAnalysisOption == VoiceAnalysisOption.MACHINE_LEARNING &&
                it.second.size == MachineLearning.DATA_SIZE

    private fun addData(
        accumulator: Pair<Array<Byte>, Array<Short>>,
        newData: ByteArray
    ): Pair<Array<Byte>, Array<Short>> {
        return accumulator.first.plus(newData.toTypedArray()) to accumulator.second
            .plus(bytesToShorts(newData).toTypedArray())
    }

    private fun newAccumulator() = Pair(emptyArray<Byte>(), emptyArray<Short>())

    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }
}
