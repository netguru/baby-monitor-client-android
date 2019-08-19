package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.common.extensions.toJson
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus
import co.netguru.baby.monitor.client.data.communication.websocket.ConnectionStatus.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
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
        private val babyNameObservable: Flowable<String>,
        private var onAvailabilityChange: (CustomWebSocketClient, ConnectionStatus) -> Unit,
        onMessageReceived: (CustomWebSocketClient, String?) -> Unit
) : WebSocketClient(URI(address)) {

    var connectionStatus: ConnectionStatus = UNKNOWN
    var wasRetrying = false
    private var onMessageReceivedListeners = mutableListOf<(CustomWebSocketClient, String?) -> Unit>()

    private val socketOpenDisposables = CompositeDisposable()

    init {
        onMessageReceivedListeners.add(onMessageReceived)
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        notifyAvailabilityChange(CONNECTED)
        babyNameObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { name ->
                    Message(babyName = name)
                            .toJson()
                            .let(::sendMessage)
                }
                .addTo(socketOpenDisposables)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
        notifyAvailabilityChange(DISCONNECTED)
        socketOpenDisposables.clear()
    }

    override fun onMessage(message: String?) {
        Timber.i("onMessage: $message")
        if (!message.isNullOrEmpty()) {
            for (listener in onMessageReceivedListeners) {
                listener(this, message)
            }
        }
    }

    override fun onMessage(bytes: ByteBuffer?) {
        bytes ?: return
        val buffer = ByteArray(bytes.remaining())
        bytes.get(buffer)
        Timber.i("onMessage received bytes: ${buffer.size}")
        onMessage(String(buffer, Charset.defaultCharset()))
    }

    override fun onError(ex: Exception?) {
        Timber.e("onError: $ex")
        notifyAvailabilityChange(DISCONNECTED)
    }

    fun asyncReconnect() = Completable.fromAction {
        reconnect()
        notifyAvailabilityChange(RETRYING)
    }

    fun connectClient() = Completable.fromAction {
        connectBlocking()
    }

    fun onDestroy() {
        onAvailabilityChange = { _, _ -> }
        onMessageReceivedListeners = mutableListOf()
    }

    fun sendMessage(message: String) {
        Timber.i("send: $message")
        if (connectionStatus == CONNECTED) {
            send(message)
        }
    }

    fun addMessageListener(listener: (CustomWebSocketClient, String?) -> Unit) {
        onMessageReceivedListeners.add(listener)
    }

    fun removeMessageListener(listener: (CustomWebSocketClient, String?) -> Unit) =
            onMessageReceivedListeners.remove(listener)

    fun closeClient() = Completable.fromAction {
        close(CloseFrame.FLASHPOLICY, "")
    }

    fun notifyAvailabilityChange(availability: ConnectionStatus) {
        if (availability != this.connectionStatus) {
            this.connectionStatus = availability
            when (availability) {
                RETRYING -> wasRetrying = true
                CONNECTED -> wasRetrying = false
            }
            onAvailabilityChange(this, availability)
        }
    }
}
