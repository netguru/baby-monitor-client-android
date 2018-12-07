package co.netguru.baby.monitor.client.feature.machinelearning

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import io.reactivex.Observable

class AacRecorder {

    private var bufferSize = 0
    private var audioRecord: AudioRecord? = null
    private var shouldStopRecording = false

    fun initRecorder() = Observable.create<ByteArray> { emitter ->
        try {
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
                emitter.onNext(buffer)
            }
        } catch (e: Exception) {
            emitter.onError(e)
        }
        emitter.onComplete()
    }

    fun release() {
        shouldStopRecording = true
        audioRecord?.release()
    }

    companion object {
        internal const val SAMPLING_RATE = 44_100
    }
}
