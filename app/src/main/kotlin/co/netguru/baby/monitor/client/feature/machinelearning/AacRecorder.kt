package co.netguru.baby.monitor.client.feature.machinelearning

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import co.netguru.baby.monitor.client.common.RunsInBackground
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AacRecorder {

    internal val data: PublishSubject<Pair<ByteArray, ShortArray>> = PublishSubject.create()
    private var bufferSize = 0
    private var audioRecord: AudioRecord? = null
    private var shouldStopRecording = false
    private var rawData = emptyArray<Byte>()
    private var newData = emptyArray<Short>()

    fun startRecording(): Completable = Completable.fromAction {
        Timber.i("starting recording")
        bufferSize = AudioRecord.getMinBufferSize(
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLING_RATE * 2
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        Timber.i("recording started")
        val buffer = ByteArray(bufferSize)
        while (!shouldStopRecording) {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_STOPPED) audioRecord?.startRecording()
            audioRecord?.read(buffer, 0, buffer.size)
            addData(buffer)
        }
    }

    fun release() {
        shouldStopRecording = true
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    @RunsInBackground
    fun addData(array: ByteArray) {
        newData = newData.plus(bytesToShorts(array).toTypedArray())
        rawData = rawData.plus(array.toTypedArray())

        if (newData.size >= MachineLearning.DATA_SIZE) {
            data.onNext(rawData.toByteArray() to newData.toShortArray())
            newData = emptyArray()
            rawData = emptyArray()
        }
    }

    @RunsInBackground
    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    companion object {
        internal const val SAMPLING_RATE = 44_100
        internal const val CHANNELS = 1
        internal const val BIT_RATE = 16
    }
}
