package co.netguru.baby.monitor.client.feature.communication.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.Randomiser
import java.net.URI
import javax.inject.Inject

class PairingViewModel @Inject constructor(
    private val pairingUseCase: PairingUseCase,
    randomiser: Randomiser
) : ViewModel() {

    internal val pairingCompletedState: LiveData<Boolean> = pairingUseCase.pairingCompletedState
    val randomPairingCode =
        randomiser.getRandomDigits(NUMBER_OF_DIGITS_PAIRING_CODE).joinToString("")

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
    }
}
