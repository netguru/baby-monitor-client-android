package co.netguru.baby.monitor.client.feature.machinelearning

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.WorkerThread
import co.netguru.baby.monitor.client.feature.common.DataBounder
import co.netguru.baby.monitor.client.feature.common.extensions.saveAssetToCache
import co.netguru.baby.monitor.client.feature.machinelearning.AacRecorder.Companion.SAMPLING_RATE
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MachineLearning(
        context: Context
) {

    val data: PublishSubject<Map<String, Float>> = PublishSubject.create()
    val result = MutableLiveData<DataBounder<Array<Float>>>()
    private val inferenceInterface = TensorFlowInferenceInterface(
            context.assets,
            "tiny_conv_dataset.pb"
    )
    private val sampleRateList = intArrayOf(SAMPLING_RATE)
    private var newData = emptyArray<Short>()

    fun feedData(array: ByteArray): Single<MutableMap<String, Float>>? {
        return feedData(bytesToShorts(array))
    }

    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    fun feedData(array: ShortArray): Single<MutableMap<String, Float>>? {
        newData = newData.plus(array.toTypedArray())
        if (newData.size <= DATA_SIZE) {
            return null
        }

        return Single.just(newData).map { data ->
            newData = emptyArray()
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
            return@map mapAndPostData(outputScores.toTypedArray())
        }
    }

    private fun mapAndPostData(floats: Array<Float>): MutableMap<String, Float> {
        val map = mutableMapOf<String, Float>()
        map[OUTPUT_1_SILENCE] = floats[0]
        map[OUTPUT_2_BACKGROUND_NOISE] = floats[1]
        map[OUTPUT_3_CRYING_BABY] = floats[2]
        map[OUTPUT_4_NOISE] = floats[3]
        Timber.i("data: $map")
        return map
    }

    companion object {
        internal const val DATA_SIZE = 441_000
        private const val INPUT_DATA_NAME = "decoded_sample_data:0"
        private const val SAMPLE_RATE_NAME = "decoded_sample_data:1"
        private const val OUTPUT_SCORES_NAME = "labels_softmax"

        private const val OUTPUTS_NUMBER = 4
        const val OUTPUT_1_SILENCE = "SILENCE"
        const val OUTPUT_2_BACKGROUND_NOISE = "BACKGROUND_NOISE"
        const val OUTPUT_3_CRYING_BABY = "CRYING_BABY"
        const val OUTPUT_4_NOISE = "NOISE"

        private val outputScoresNames = arrayOf(OUTPUT_SCORES_NAME)
    }
}
