package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.communication.websocket.ConnectionStatus.*
import io.reactivex.Completable
import org.java_websocket.client.WebSocketClient
import org.java_websocket.framing.CloseFrame
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomWebSocketClient(
        val address: String,
        private var onAvailabilityChange: (CustomWebSocketClient, ConnectionStatus) -> Unit,
        onMessageReceived: (CustomWebSocketClient, String?) -> Unit
) : WebSocketClient(URI(address)) {

    var connectionStatus: ConnectionStatus = UNKNOWN
    var wasRetrying = false
    private var onMessageReceivedListeners = mutableListOf<(CustomWebSocketClient, String?) -> Unit>()

    init {
        onMessageReceivedListeners.add(onMessageReceived)
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

    fun asyncReconnect() = Completable.fromAction {
        reconnect()
        notifyAvailabilityChange(RETRYING)
    }

    fun connectClient() = Completable.fromAction {
        connect()
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
            Timber.e("$this connection change: $availability")
            onAvailabilityChange(this, availability)
        }
    }
}
