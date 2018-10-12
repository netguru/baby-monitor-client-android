package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import co.netguru.baby.monitor.client.common.extensions.subscribeWithLiveData
import co.netguru.baby.monitor.client.feature.common.DataBounder
import io.reactivex.Single
import io.reactivex.SingleSource
import io.reactivex.schedulers.Schedulers
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MachineLearning(
        context: Context,
        sampleRate: Int
) {

    val inferenceInterface = TensorFlowInferenceInterface(
            context.assets,
            "my_frozen_graph.pb"
    )
    private val sampleRateList = intArrayOf(sampleRate)
    val result = MutableLiveData<DataBounder<FloatArray>>()


    init {
        /* context.saveAssetToCache("crying_baby_1.wav")
                 .subscribeOn(Schedulers.io())
                 .subscribe(object : SingleObserver<File> {
                     override fun onSuccess(file: File) {
                         val inputStream = file.inputStream()
                         val data = convertStreamToByteArray(inputStream)
                         inputStream.close()
                         feedData(data).observeForever {
                             Timber.e(it.toJson())
                         }

                         *//*var outputScores = FloatArray(4)
                        try {
                            inferenceInterface.feed("wav_data", file.absolutePath.toByteArray())
                            inferenceInterface.run(outputScoresNames)
                            inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores)
                            Timber.e(outputScores.toJson())
                        } catch (e: Exception) {
                            Timber.e(e)
                        }*//*
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Timber.e(e)
                    }

                })*/
    }

    fun convertStreamToByteArray(inputStream: InputStream): ShortArray {
        val baos = ByteArrayOutputStream()
        val buff = ByteArray(10240)
        var i: Int
        while (inputStream.available() > 1) {
            i = inputStream.read(buff, 0, buff.size)
            baos.write(buff, 0, i)
        }
        val bytes = baos.toByteArray()
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)

        return shorts
    }

    fun feedData(data: ShortArray) = Single.defer {
        SingleSource<FloatArray> { observer ->
            var outputScores = FloatArray(6)
            val mappedData = data.map {
                if (it >= 0) {
                    it / Short.MAX_VALUE.toFloat()
                } else {
                    it / (Short.MIN_VALUE.toFloat() * -1)
                }
            }.toFloatArray()
            try {
                inferenceInterface.feed(SAMPLE_RATE_NAME, sampleRateList)
                inferenceInterface.feed(INPUT_DATA_NAME, mappedData, data.size.toLong(), 1)
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
        private const val INPUT_DATA_NAME = "decoded_sample_data:0"
        private const val SAMPLE_RATE_NAME = "decoded_sample_data:1"
        private const val OUTPUT_SCORES_NAME = "labels_softmax"

        private val outputScoresNames = arrayOf(OUTPUT_SCORES_NAME)
        private val labels = arrayOf(
                "crying_baby",
                "noise",
                "baby_laugh"
        )
    }
}
