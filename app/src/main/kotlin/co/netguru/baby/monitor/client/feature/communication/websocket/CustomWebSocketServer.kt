package co.netguru.baby.monitor.client.feature.communication.websocket

import io.reactivex.Completable
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomWebSocketServer(
        port: Int? = null,
        private val onMessageReceived: (WebSocket?, String?) -> Unit,
        private val onErrorListener: (Exception) -> Unit
) : WebSocketServer(InetSocketAddress(port ?: PORT)) {

    init {
        isReuseAddr = true
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Timber.i("onOpen: ${conn?.remoteSocketAddress?.address?.hostAddress}")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Timber.i(message)
        onMessageReceived(conn, message)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        message ?: return
        Timber.i("byte message")
        val buffer = ByteArray(message.remaining())
        message.get(buffer)
        onMessage(conn, String(buffer, Charset.defaultCharset()))
    }

    override fun onStart() {
        Timber.i("CustomWebSocketServer started")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Timber.e("onError: $ex")
        ex?.let(onErrorListener)
        stopServer()
    }

    fun startServer() = Completable.fromAction {
        start()
    }

    fun stopServer() = Completable.fromAction {
        stop()
    }

    companion object {
        internal const val PORT = 63124
    }
}
