package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.feature.batterylevel.NotifyLowBatteryUseCase
import dagger.Lazy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber
import javax.inject.Inject

class ChildMonitorViewModel @Inject constructor(
    private val receiveFirebaseTokenUseCase: Lazy<ReceiveFirebaseTokenUseCase>,
    private val notifyLowBatteryUseCase: NotifyLowBatteryUseCase
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

    fun notifyLowBattery(title: String, text: String) {
        notifyLowBatteryUseCase.notifyLowBattery(title = title, text = text)
            .subscribeBy(
                onComplete = {
                    Timber.d("Low battery notification posted.")
                },
                onError = { error ->
                    Timber.w(error, "Error posting low battery notification.")
                }
            )
            .addTo(disposables)
    }

    override fun onCleared() {
        disposables.clear()
    }
}
