package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.communication.websocket.ConnectionStatus.*
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomWebSocketClient(
        val address: String,
        private val communicationListener: WebSocketCommunicationListener
) : WebSocketClient(URI(address)) {

    var availability: ConnectionStatus = UNKNOWN
    private val compositeDisposable = CompositeDisposable()
    private val onMessageReceivedListeners = mutableListOf<(CustomWebSocketClient, String?) -> Unit>()

    override fun connect() {
        Completable.fromAction {
            super.connect()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.i("Complete")
                },
                onError = {
                    Timber.e("connection error: $it")
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
        if (!message.isNullOrEmpty()) {
            for (listener in onMessageReceivedListeners) {
                listener(this, message)
            }

            communicationListener.onMessageReceived(this, message)
        }
    }

    override fun onMessage(bytes: ByteBuffer?) {
        bytes ?: return
        val buffer = ByteArray(bytes.remaining())
        bytes.get(buffer)
        onMessage(String(buffer, Charset.defaultCharset()))
    }

    override fun onError(ex: Exception?) {
        Timber.e("onError: $ex")
        notifyAvailabilityChange(DISCONNECTED)
    }

    fun onDestroy() {
        closeClient()
        compositeDisposable.dispose()
    }

    fun sendMessage(message: String) {
        Timber.i("send: $message")
        if (availability == CONNECTED) {
            send(message)
        }
    }

    fun addMessageListener(listener: (CustomWebSocketClient, String?) -> Unit) {
        onMessageReceivedListeners.add(listener)
    }

    fun removeListener(listener: (CustomWebSocketClient, String?) -> Unit) =
            onMessageReceivedListeners.remove(listener)


    private fun closeClient() {
        Completable.fromAction {
            close(CloseFrame.FLASHPOLICY, "")
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.i("Closed")
                },
                onError = {
                    notifyAvailabilityChange(DISCONNECTED)
                }
        ).addTo(compositeDisposable)
    }

    private fun notifyAvailabilityChange(availability: ConnectionStatus) {
        if (availability != this.availability) {
            this.availability = availability
            communicationListener.onAvailabilityChanged(this, availability)
        }
    }
}
