package co.netguru.baby.monitor.client.feature.machinelearning

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import co.netguru.baby.monitor.client.feature.common.RunsInBackground
import io.reactivex.Completable
import io.reactivex.subjects.PublishSubject
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AacRecorder {

    internal val data: PublishSubject<ShortArray> = PublishSubject.create()
    private var bufferSize = 0
    private var audioRecord: AudioRecord? = null
    private var shouldStopRecording = false
    private var newData = emptyArray<Short>()

    fun startRecording(): Completable = Completable.fromAction {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 2
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
        audioRecord?.startRecording()

        val buffer = ByteArray(bufferSize)
        while (!shouldStopRecording) {
            audioRecord?.read(buffer, 0, buffer.size)
            addData(buffer)
        }
    }

    fun release() {
        shouldStopRecording = true
        audioRecord?.release()
    }

    @RunsInBackground
    fun addData(array: ByteArray) {
        addData(bytesToShorts(array))
    }

    @RunsInBackground
    private fun bytesToShorts(bytes: ByteArray): ShortArray {
        val shorts = ShortArray(bytes.size / 2)
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
        return shorts
    }

    @RunsInBackground
    fun addData(array: ShortArray) {
        newData = newData.plus(array.toTypedArray())
        if (newData.size >= MachineLearning.DATA_SIZE) {
            data.onNext(newData.toShortArray())
            newData = emptyArray()
        }
    }

    companion object {
        internal const val SAMPLING_RATE = 44_100
    }
}
