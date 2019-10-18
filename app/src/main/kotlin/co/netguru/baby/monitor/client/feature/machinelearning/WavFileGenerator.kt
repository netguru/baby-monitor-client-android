package co.netguru.baby.monitor.client.feature.machinelearning

import android.content.Context
import io.reactivex.Single
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import timber.log.Timber
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

@Suppress("MagicNumber")
object WavFileGenerator {

    internal const val DIRECTORY_NAME = "recordings"
    internal const val DATE_PATTERN = "yyyy_MM_dd_HH_mm_ss"

    private const val BYTES_IN_MEGABYTE = 1_048_576L
    private const val AVAILABLE_MEGABYTES_FOR_APPLICATION = 200
    private const val AVAILABLE_SPACE = AVAILABLE_MEGABYTES_FOR_APPLICATION * BYTES_IN_MEGABYTE

    fun saveAudio(
        context: Context,
        rawData: ByteArray,
        bitsPerSample: Byte,
        channels: Int,
        sampleRate: Int,
        byteRate: Int
    ) = Single.fromCallable {
        checkAvailableSpace(context)
        val formatter = DateTimeFormatter.ofPattern(DATE_PATTERN)
        val file = File(
            context.getDir(DIRECTORY_NAME, Context.MODE_PRIVATE),
            "crying_${LocalDateTime.now().format(formatter)}.wav"
        )

        DataOutputStream(FileOutputStream(file)).use { output ->
            writeString(output, "RIFF") // chunk id
            writeInt(output, 36 + rawData.size) // chunk size
            writeString(output, "WAVE") // format
            writeString(output, "fmt ") // subchunk 1 id
            writeInt(output, 16) // subchunk 1 size
            writeShort(output, 1.toShort()) // audio format (1 = PCM)
            writeShort(output, 1.toShort()) // number of channels
            writeInt(output, sampleRate) // sample rate
            writeInt(output, byteRate) // byte rate
            writeShort(output, 2.toShort()) // block align
            writeShort(output, 16.toShort()) // bits per sample
            writeString(output, "data") // subchunk 2 id
        }

        FileOutputStream(file).use { steam ->
            steam.write(
                getHeader(
                    rawData, bitsPerSample,
                    channels, sampleRate, byteRate
                ).toByteArray()
            )
            steam.write(rawData)
        }
        Timber.i("File saved ${file.absolutePath}")
        true
    }

    private fun writeInt(output: DataOutputStream, value: Int) {
        output.write(value)
        output.write(value shr 8)
        output.write(value shr 16)
        output.write(value shr 24)
    }

    private fun writeShort(output: DataOutputStream, value: Short) {
        output.write(value.toInt())
        output.write(value.toInt() shr 8)
    }

    private fun writeString(output: DataOutputStream, value: String) {
        for (i in 0 until value.length) {
            output.write(value[i].toInt())
        }
    }

    /**
     *
     * WAVE header
     * see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
     */
    private fun getHeader(
        rawData: ByteArray,
        bitsPerSample: Byte,
        channels: Int,
        sampleRate: Int,
        byteRate: Int
    ) = arrayOf(
        'R'.toByte(),
        'I'.toByte(),
        'F'.toByte(),
        'F'.toByte(),
        (rawData.size + 36 and 0xff).toByte(),
        (rawData.size + 36 shr 8 and 0xff).toByte(),
        (rawData.size + 36 shr 16 and 0xff).toByte(),
        (rawData.size + 36 shr 24 and 0xff).toByte(),
        'W'.toByte(),
        'A'.toByte(),
        'V'.toByte(),
        'E'.toByte(),
        'f'.toByte(),
        'm'.toByte(),
        't'.toByte(),
        ' '.toByte(),
        16,
        0,
        0,
        0,
        1,
        0,
        channels.toByte(),
        0,
        (sampleRate and 0xff).toByte(),
        (sampleRate shr 8 and 0xff).toByte(),
        (sampleRate shr 16 and 0xff).toByte(),
        (sampleRate shr 24 and 0xff).toByte(),
        (byteRate and 0xff).toByte(),
        (byteRate shr 8 and 0xff).toByte(),
        (byteRate shr 16 and 0xff).toByte(),
        (byteRate shr 24 and 0xff).toByte(),
        (2 * 16 / 8).toByte(),
        0,
        bitsPerSample,
        0,
        'd'.toByte(),
        'a'.toByte(),
        't'.toByte(),
        'a'.toByte(),
        (rawData.size and 0xff).toByte(),
        (rawData.size shr 8 and 0xff).toByte(),
        (rawData.size shr 16 and 0xff).toByte(),
        (rawData.size shr 24 and 0xff).toByte()
    )

    private fun checkAvailableSpace(context: Context) {
        val directory = context.getDir(DIRECTORY_NAME, Context.MODE_PRIVATE)
        var size = 0L
        for (file in directory.listFiles()) {
            size += file.length()
        }

        if (size > AVAILABLE_SPACE) {
            directory
                .listFiles()
                .sortedBy { file -> file.lastModified() }
                .firstOrNull()?.delete()
            checkAvailableSpace(context)
        }
    }
}
