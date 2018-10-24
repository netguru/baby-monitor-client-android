package co.netguru.baby.monitor.client.feature.websocket

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import co.netguru.baby.monitor.client.feature.websocket.ConnectionStatus.*
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI

class CustomWebSocketClient(
        serverUrl: String,
        private val onAvailabilityChange: (ConnectionStatus) -> Unit
) : WebSocketClient(URI(serverUrl)), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private var availability: ConnectionStatus = UNKNOWN

    init {
        Single.fromCallable {
            connect()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onSuccess = {
                    Timber.i("Success")
                },
                onError = {
                    notifyAvailabilityChange(DISCONNECTED)
                }
        ).addTo(compositeDisposable)
    }

    fun closeClient() {
        Single.fromCallable{
            close(1000)
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onSuccess = {
                    Timber.i("Closed")
                },
                onError = {
                    notifyAvailabilityChange(DISCONNECTED)
                }
        ).addTo(compositeDisposable)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        notifyAvailabilityChange(CONNECTED)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
        notifyAvailabilityChange(DISCONNECTED)
    }

    override fun onMessage(message: String?) {
        Timber.i("onMessage: $message")
    }

    override fun onError(ex: Exception?) {
        Timber.e(ex)
        notifyAvailabilityChange(DISCONNECTED)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy() {
        compositeDisposable.dispose()
    }

    private fun notifyAvailabilityChange(availability: ConnectionStatus) {
        if (availability != this.availability) {
            this.availability = availability
            onAvailabilityChange(availability)
        }
    }
}
