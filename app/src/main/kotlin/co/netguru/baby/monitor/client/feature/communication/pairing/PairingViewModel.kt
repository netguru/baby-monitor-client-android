package co.netguru.baby.monitor.client.feature.communication.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import java.net.URI
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class PairingViewModel @Inject constructor(
    private val pairingUseCase: PairingUseCase
) : ViewModel() {

    internal val pairingCompletedState: LiveData<Boolean> = pairingUseCase.pairingCompletedState
    val randomPairingCode =
        List(NUMBER_OF_DIGITS_PAIRING_CODE) { Random.nextInt(PARING_CODE_DIGITS_RANGE) }
            .joinToString("")

    fun pair(address: URI) {
        pairingUseCase.pair(address, randomPairingCode)
    }

    fun cancelPairing() {
        pairingUseCase.cancelPairing()
    }

    override fun onCleared() {
        pairingUseCase.dispose()
        super.onCleared()
    }

    companion object {
        private const val NUMBER_OF_DIGITS_PAIRING_CODE = 4
        private val PARING_CODE_DIGITS_RANGE = 0..9
    }
}
