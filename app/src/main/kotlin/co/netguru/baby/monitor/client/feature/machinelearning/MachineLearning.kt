package co.netguru.baby.monitor.client.feature.machinelearning

import android.content.Context
import co.netguru.baby.monitor.client.feature.machinelearning.AacRecorder.Companion.SAMPLING_RATE
import io.reactivex.Single
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber

class MachineLearning(context: Context) {

    private val inferenceInterface = TensorFlowInferenceInterface(
            context.assets,
            "model_exp76.pb"
    )
    private val sampleRateList = intArrayOf(SAMPLING_RATE)

    fun processData(array: ShortArray) = Single.just(array).map { data ->
        val outputScores = FloatArray(OUTPUTS_NUMBER)
        val mappedData = FloatArray(DATA_SIZE) {
            if (data.size < it) return@FloatArray 0f

            return@FloatArray if (data[it] >= 0) {
                data[it].toFloat() / Short.MAX_VALUE
            } else {
                data[it].toFloat() / (Short.MIN_VALUE) * -1
            }
        }
        with(inferenceInterface) {
            feed(SAMPLE_RATE_NAME, sampleRateList)
            feed(INPUT_DATA_NAME, mappedData, DATA_SIZE.toLong(), 1)
            run(outputScoresNames, true)
            fetch(OUTPUT_SCORES_NAME, outputScores)
        }
        return@map mapData(outputScores.toTypedArray())
    }

    private fun mapData(floats: Array<Float>): MutableMap<String, Float> {
        val map = mutableMapOf<String, Float>()
        map[OUTPUT_1_NOISE] = floats[0]
        map[OUTPUT_2_CRYING_BABY] = floats[1]
        Timber.i("data: $map")
        return map
    }

    companion object {
        internal const val DATA_SIZE = 176_400
        private const val INPUT_DATA_NAME = "raw_audio:0"
        private const val SAMPLE_RATE_NAME = "sample_rate:0"
        private const val OUTPUT_SCORES_NAME = "labels_softmax"

        private const val OUTPUTS_NUMBER = 2
        const val OUTPUT_1_NOISE = "NOISE"
        const val OUTPUT_2_CRYING_BABY = "CRYING_BABY"

        const val CRYING_THRESHOLD = 0.9

        private val outputScoresNames = arrayOf(OUTPUT_SCORES_NAME)
    }
}
