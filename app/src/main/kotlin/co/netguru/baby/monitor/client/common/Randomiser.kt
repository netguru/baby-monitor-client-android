package co.netguru.baby.monitor.client.common

import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class Randomiser @Inject constructor() {

    fun getRandomDigits(numberOfDigits: Int) =
        List(numberOfDigits) { Random.nextInt(RANDOM_DIGIT_RANGE) }

    companion object {
        private val RANDOM_DIGIT_RANGE = 0..9
    }
}
