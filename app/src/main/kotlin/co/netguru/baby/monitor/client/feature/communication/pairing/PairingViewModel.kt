package co.netguru.baby.monitor.client.feature.communication.pairing

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import java.net.URI
import javax.inject.Inject
import kotlin.random.Random
import kotlin.random.nextInt

class PairingViewModel @Inject constructor(
    private val pairingUseCase: PairingUseCase
) : ViewModel() {

    internal val pairingCompletedState: LiveData<Boolean> = pairingUseCase.pairingCompletedState
    private val compositeDisposable = CompositeDisposable()
    val randomPairingCode = List(4) { Random.nextInt(0..9) }
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
}
