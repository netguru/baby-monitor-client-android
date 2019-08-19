package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import dagger.Lazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class ChildMonitorViewModel @Inject constructor(
    private val receiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase>
) : ViewModel() {

    private val disposables = CompositeDisposable()

    fun receiveFirebaseToken(ipAddress: String, token: String) {
        receiveFirebaseTokenUseCase.get().receiveToken(ipAddress = ipAddress, token = token)
            .subscribeBy(
                onComplete = {
                    Timber.d("Firebase token saved for address $ipAddress.")
                },
                onError = { error ->
                    Timber.w(error, "Couldn't save Firebase token.")
                }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
    }
}
