package co.netguru.baby.monitor.client.feature.noisedetection

import co.netguru.baby.monitor.client.feature.machinelearning.MachineLearning
import co.netguru.baby.monitor.client.feature.voiceAnalysis.AacRecorder.Companion.SAMPLING_RATE
import io.reactivex.Single
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.log10
import kotlin.math.roundToInt
import kotlin.math.sqrt

class NoiseDetector @Inject constructor() {
    fun processData(data: ShortArray) = Single.fromCallable<Int> {
        val maxDecibels = data.asSequence()
            .chunked(ANALYZE_DATA_CHUNK_SIZE)
            .map(this::mapToDecibels)
            .max()

        Timber.d("Max Db: $maxDecibels")
        return@fromCallable maxDecibels?.roundToInt() ?: 0
    }

    private fun mapToDecibels(shorts: List<Short>): Double {
        var totalSquared = 0.0
       shorts.forEach {
               totalSquared += (it * it).toDouble()
       }
        val quadraticMeanPressure = sqrt(totalSquared / shorts.size)
        return FORMULA_CONSTANT * log10(quadraticMeanPressure)
    }

    companion object {
        internal const val DATA_SIZE = MachineLearning.DATA_SIZE
        private const val ANALYZE_DATA_CHUNK_SIZE = SAMPLING_RATE / 10
        const val DEFAULT_NOISE_LEVEL = 65
        private const val FORMULA_CONSTANT = 20
    }
}
