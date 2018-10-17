package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import co.netguru.baby.monitor.client.common.extensions.saveAssetToCache
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.common.DataBounder
import io.reactivex.Single
import io.reactivex.SingleObserver
import io.reactivex.SingleSource
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MachineLearning(
        context: Context,
        sampleRate: Int
) {

    val inferenceInterface = TensorFlowInferenceInterface(
            context.assets,
            "tiny_conv_new_dataset.pb"
    )
    private val sampleRateList = intArrayOf(sampleRate)
    private var newData = emptyArray<Short>()
    val result = MutableLiveData<DataBounder<FloatArray>>()

    init {
        testFile(context, "noise_407.wav")
    }

    private fun testFile(context: Context, file: String) {
        context.saveAssetToCache(file)
                .subscribeOn(Schedulers.io())
                .subscribe(object : SingleObserver<File> {
                    override fun onSuccess(file: File) {
                        val inputStream = file.inputStream()
                        val data = convertStreamToShortData(inputStream)
                        inputStream.close()
                        feedData(data)
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
                    }

                })
    }

    fun convertStreamToShortData(inputStream: InputStream): ShortArray {
        val baos = ByteArrayOutputStream()
        val buff = ByteArray(10240)
        var i: Int
        while (inputStream.available() > 1) {
            i = inputStream.read(buff, 0, buff.size)
            baos.write(buff, 0, i)
        }
        var bytes = baos.toByteArray()
        for (j in 0 until bytes.size) {
            if (bytes[j].toInt() == 0x64 && bytes[j + 1].toInt() == 0x61
                    && bytes[j + 2].toInt() == 0x74 && bytes[j + 3].toInt() == 0x61) {
                bytes = bytes.drop(j + 4).toByteArray()
                break
            }
        }
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    fun feedData(data: ShortArray) = Single.defer {
        SingleSource<FloatArray> { observer ->
            newData = newData.plus(data.toTypedArray())
            if (newData.size <= DATA_SIZE) {
                return@SingleSource
            }
            val outputScores = FloatArray(labels.size)
            val mappedData = FloatArray(DATA_SIZE) {
                if (data.size < it) return@FloatArray 0f

                return@FloatArray if (data[it] >= 0) {
                    data[it].toFloat() / Short.MAX_VALUE
                } else {
                    data[it].toFloat() / (Short.MIN_VALUE) * -1
                }
            }
            try {
                inferenceInterface.feed(SAMPLE_RATE_NAME, sampleRateList)
                inferenceInterface.feed(INPUT_DATA_NAME, mappedData, DATA_SIZE.toLong(), 1)
                inferenceInterface.run(outputScoresNames, true)
                inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores)
                observer.onSuccess(outputScores)
            } catch (e: Exception) {
                Timber.e(e)
                observer.onError(e)
            }
        }
    }.subscribeOn(Schedulers.io()).subscribeWithLiveData(result)

    companion object {
        internal const val DATA_SIZE = 441_000
        private const val INPUT_DATA_NAME = "decoded_sample_data:0"
        private const val SAMPLE_RATE_NAME = "decoded_sample_data:1"
        private const val OUTPUT_SCORES_NAME = "labels_softmax"

        private val outputScoresNames = arrayOf(OUTPUT_SCORES_NAME)
        private val labels = arrayOf(
                "silence",
                "unknown",
                "crying_baby",
                "noise"
        )
    }
}
