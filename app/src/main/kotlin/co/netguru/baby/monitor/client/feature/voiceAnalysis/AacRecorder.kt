package co.netguru.baby.monitor.client.feature.voiceAnalysis

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class AacRecorder @Inject constructor() {

    fun startRecording(): Observable<ByteArray> = Observable.create { emitter ->
        Timber.i("starting recording")
        var shouldStopRecording = false
        val bufferSize = findBestBufferSize()
        val audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        Timber.i("recording started")
        val buffer = ByteArray(bufferSize)

        emitter.setCancellable {
            shouldStopRecording = true
            audioRecord.stop()
            audioRecord.release()
            Timber.i("release")
        }

        while (!shouldStopRecording) {
            if (audioRecord.recordingState == AudioRecord.RECORDSTATE_STOPPED) audioRecord.startRecording()
            audioRecord.read(buffer, 0, buffer.size)
            emitter.onNext(buffer)
        }
    }

    private fun findBestBufferSize(): Int {
        var bufferSize = AudioRecord.getMinBufferSize(
            SAMPLING_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ) * 2
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLING_RATE * 2
        }
        return bufferSize
    }

    companion object {
        internal const val SAMPLING_RATE = 44_100
        internal const val CHANNELS = 1
        internal const val BITS_PER_SAMPLE = 16
    }
}
