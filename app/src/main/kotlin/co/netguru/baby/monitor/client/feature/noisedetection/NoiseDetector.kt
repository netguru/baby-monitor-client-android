package co.netguru.baby.monitor.client.feature.noisedetection

import co.netguru.baby.monitor.client.feature.voiceAnalysis.AacRecorder.Companion.SAMPLING_RATE
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sqrt

class NoiseDetector @Inject constructor() {
    fun processData(data: ShortArray) = Single.fromCallable<Int> {
        var decibels = 0.0
        if (data.isNotEmpty()) {
            var totalSquared = 0.0
            for (soundBit in data) {
                totalSquared += (soundBit * soundBit).toDouble()
            }

            val quadraticMeanPressure = sqrt(totalSquared / data.size)
            decibels = FORMULA_CONSTANT * log10(quadraticMeanPressure)
        }
        Timber.d("Sound Db: $decibels")
        return@fromCallable decibels.roundToInt()
    }

    companion object {
        internal const val DATA_SIZE = SAMPLING_RATE / 10
        const val DEFAULT_NOISE_LEVEL = 65
        private const val FORMULA_CONSTANT = 20
    }
}
