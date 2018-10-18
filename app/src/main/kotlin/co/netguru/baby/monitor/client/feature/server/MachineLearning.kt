package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.support.annotation.WorkerThread
import co.netguru.baby.monitor.client.common.extensions.saveAssetToCache
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.common.DataBounder
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
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
    val result = MutableLiveData<DataBounder<FloatArray>>()
    private val compositeDisposable = CompositeDisposable()
    private val sampleRateList = intArrayOf(sampleRate)
    private var newData = emptyArray<Short>()

    init {
        testFile(context, TEST_FILE)
    }

    fun feedData(array: ShortArray) {
        newData = newData.plus(array.toTypedArray())
        if (newData.size <= DATA_SIZE) {
            return
        }

        Single.just(newData).map { data ->
            val outputScores = FloatArray(labels.size)
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
            return@map outputScores
        }.subscribeOn(Schedulers.io()).subscribeWithLiveData(result)
        newData = emptyArray()
    }

    fun dispose() {
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    //TODO when ML model will be ready remove this function and everything connected to it
    private fun testFile(context: Context, fileName: String) {
        context.saveAssetToCache(fileName)
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onSuccess = {
                            FileInputStream(it).use {
                                val data = convertStreamToShortData(it)
                                feedData(data)
                            }
                        },
                        onError = {
                            Timber.e(it)
                        }
                ).also {
                    compositeDisposable.add(it)
                }
    }

    @WorkerThread
    private fun convertStreamToShortData(inputStream: InputStream): ShortArray {
        var bytes = getByteArrayOutputStream(inputStream).use {
            it.toByteArray()
        }
        for (j in 0 until bytes.size) {
            if (areNextBytesProper(bytes, j)) {
                bytes = bytes.drop(j + 4).toByteArray()
                break
            }
        }

        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    @WorkerThread
    private fun getByteArrayOutputStream(inputStream: InputStream): ByteArrayOutputStream {
        val baos = ByteArrayOutputStream()
        val buff = ByteArray(10240)
        while (inputStream.available() > 1) {
            baos.write(buff, 0, inputStream.read(buff, 0, buff.size))
        }
        return baos
    }

    private fun areNextBytesProper(byteArray: ByteArray, index: Int): Boolean =
            (byteArray[index].toInt() == 0x64 && byteArray[index + 1].toInt() == 0x61
                    && byteArray[index + 2].toInt() == 0x74 && byteArray[index + 3].toInt() == 0x61)

    companion object {
        internal const val DATA_SIZE = 441_000
        private const val INPUT_DATA_NAME = "decoded_sample_data:0"
        private const val SAMPLE_RATE_NAME = "decoded_sample_data:1"
        private const val OUTPUT_SCORES_NAME = "labels_softmax"
        private const val TEST_FILE = "noise_407.wav"

        private val outputScoresNames = arrayOf(OUTPUT_SCORES_NAME)
        private val labels = arrayOf(
                "silence",
                "unknown",
                "crying_baby",
                "noise"
        )
    }
}
