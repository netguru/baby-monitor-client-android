package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import android.content.Context
import co.netguru.baby.monitor.client.feature.communication.nsd.NsdServiceManager
import co.netguru.baby.monitor.client.feature.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcReceiver
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class ServerViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager
) : ViewModel() {

    internal var currentCall: RtcReceiver? = null
    private val compositeDisposable = CompositeDisposable()

    internal fun registerNsdService() {
        nsdServiceManager.registerService()
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun hangUp() = currentCall?.hangUp()

    internal fun accept(rtcReceiver: RtcReceiver, context: Context, onCallStateChanged: (state: CallState) -> Unit) {
        currentCall = rtcReceiver
        rtcReceiver.accept(context, onCallStateChanged)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                        onComplete = { Timber.i("completed") },
                        onError = Timber::e
                ).addTo(compositeDisposable)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
