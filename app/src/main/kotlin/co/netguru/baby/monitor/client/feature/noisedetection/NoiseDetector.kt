package co.netguru.baby.monitor.client.feature.noisedetection

import co.netguru.baby.monitor.client.feature.voiceAnalysis.AacRecorder.Companion.SAMPLING_RATE
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.sqrt

class NoiseDetector @Inject constructor() {
    fun processData(data: ShortArray, size: Int) = Single.fromCallable<Double> {
        var decibels = 0.0
        if (size > 0) {
            var totalSquared = 0.0
            for (i in 0 until size) {
                val soundBit = data[i]
                totalSquared += (soundBit * soundBit).toDouble()
            }

            val quadraticMeanPressure = sqrt(totalSquared / size)
            decibels = 20 * log10(quadraticMeanPressure)
        }
        Timber.d("Sound Db: $decibels")
        return@fromCallable decibels
    }

    companion object {
        internal const val DATA_SIZE = SAMPLING_RATE / 10
        const val DEFAULT_NOISE_THRESHOLD = 65
    }
}
