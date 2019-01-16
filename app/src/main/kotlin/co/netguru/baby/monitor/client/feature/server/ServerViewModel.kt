package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.data.splash.AppState
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ServerViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val dataRepository: DataRepository
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    internal fun registerNsdService(
            onRegistrationFailed: (errorCode: Int) -> Unit
    ) {
        nsdServiceManager.registerService(object : NsdServiceManager.OnServiceConnectedListener {
            override fun onServiceConnectionError(errorCode: Int) = Unit
            override fun onStartDiscoveryFailed(errorCode: Int) = Unit

            override fun onRegistrationFailed(errorCode: Int) {
                onRegistrationFailed(errorCode)
            }
        })
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun saveConfiguration() {
        dataRepository.saveConfiguration(AppState.SERVER)
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = { Timber.i("state saved") },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}
