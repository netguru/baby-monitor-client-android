package co.netguru.baby.monitor.client.feature.recording

import android.content.Context
import co.netguru.baby.monitor.client.feature.voiceAnalysis.AacRecorder
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject

class RecordingFileController @Inject constructor(
    private val applicationContext: Context
) {

    fun saveRecording(
        rawData: ByteArray
    ) =
        saveAudio(
            rawData,
            AacRecorder.BITS_PER_SAMPLE,
            AacRecorder.CHANNELS,
            AacRecorder.SAMPLING_RATE,
            UPLOAD_RECORDINGS_DIRECTORY
        )

    private fun saveAudio(
        rawData: ByteArray,
        bitsPerSample: Int,
        channels: Int,
        sampleRate: Int,
        directoryName: String
    ) = Single.fromCallable {
        // TODO add metadata to the recordings (should be consulted with ML dev)
        checkAvailableSpace(
            applicationContext
        )
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val fileName = "crying_${LocalDateTime.now().format(formatter)}.wav"
        val file = File(applicationContext.getDir(directoryName, Context.MODE_PRIVATE), fileName)

        file.outputStream().use { stream ->
            stream.write(
                getHeader(
                    rawData, bitsPerSample,
                    channels, sampleRate
                )
            )
            stream.write(rawData)
        }
        Timber.i("File saved ${file.absolutePath}")
        fileName
    }

    private fun getByteRate(
        bitsPerSample: Int,
        channels: Int,
        sampleRate: Int
    ) = bitsPerSample * sampleRate * channels / BITS_PER_BYTE

    private fun getBlockAlign(
        channels: Int,
        bitsPerSample: Int
    ): Short = (channels * bitsPerSample / BITS_PER_BYTE).toShort()

    /**
     *
     * WAVE header
     * see http://soundfile.sapp.org/doc/WaveFormat/
     */
    private fun getHeader(
        rawData: ByteArray,
        bitsPerSample: Int,
        channels: Int,
        sampleRate: Int
    ): ByteArray {
        return ByteBuffer
            .allocate(HEADER_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
            .put(RIFF_HEADER.toASCIIByteArray())
            .putInt(
                getChunkSize(
                    rawData
                )
            )
            .put(FORMAT.toASCIIByteArray())
            .put(SUBCHUNK_1_ID.toASCIIByteArray())
            .putInt(SUBCHUNK_1_SIZE_PCM)
            .putShort(AUDIO_FORMAT_PCM)
            .putShort(channels.toShort())
            .putInt(sampleRate)
            .putInt(
                getByteRate(
                    bitsPerSample,
                    channels,
                    sampleRate
                )
            )
            .putShort(
                getBlockAlign(
                    channels,
                    bitsPerSample
                )
            )
            .putShort(bitsPerSample.toShort())
            .put(SUBCHUNK_2_ID.toASCIIByteArray())
            .putInt(rawData.size)
            .array()
    }

    private fun String.toASCIIByteArray() = toByteArray(Charsets.US_ASCII)

    private fun getChunkSize(rawData: ByteArray) =
        CHUNK_AND_SUBCHUNK_1_DESC_SIZE + SUBCHUNK_1_SIZE_PCM + SUBCHUNK_2_ID_AND_DESC_SIZE + rawData.size

    private fun checkAvailableSpace(context: Context) {
        val directory = context.getDir(UPLOAD_RECORDINGS_DIRECTORY, Context.MODE_PRIVATE)
        var size = 0L
        for (file in directory.listFiles()) {
            size += file.length()
        }

        if (size > AVAILABLE_SPACE) {
            directory
                .listFiles()
                .sortedBy { file -> file.lastModified() }
                .firstOrNull()?.delete()
            checkAvailableSpace(
                context
            )
        }
    }

    companion object {
        internal const val UPLOAD_RECORDINGS_DIRECTORY = "recordings"
        private const val DATE_PATTERN = "yyyy_MM_dd_HH_mm_ss"

        private const val BYTES_IN_MEGABYTE = 1_048_576L
        private const val AVAILABLE_MEGABYTES_FOR_RECORDINGS = 50
        private const val AVAILABLE_SPACE = AVAILABLE_MEGABYTES_FOR_RECORDINGS * BYTES_IN_MEGABYTE
        private const val SUBCHUNK_1_SIZE_PCM = 16
        private const val RIFF_HEADER = "RIFF"
        private const val SUBCHUNK_1_ID = "fmt "
        private const val SUBCHUNK_2_ID = "data"
        private const val SUBCHUNK_2_ID_AND_DESC_SIZE = 8
        private const val CHUNK_AND_SUBCHUNK_1_DESC_SIZE = 4 + 8
        private const val FORMAT = "WAVE"
        private const val AUDIO_FORMAT_PCM: Short = 1
        private const val BITS_PER_BYTE = 8
        private const val HEADER_SIZE = 44
    }
}
