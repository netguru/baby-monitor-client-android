package co.netguru.baby.monitor.client.feature.machinelearning

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream

object WavFileGenerator {

    fun saveAudio(
            file: File,
            rawData: ByteArray,
            bitsPerSample: Byte,
            channels: Int,
            sampleRate: Int,
            byteRate: Int
    ) = Single.just(file).map { file ->
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
        return@map file
    }.subscribeOn(Schedulers.io())

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
}
