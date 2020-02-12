package co.netguru.baby.monitor.client.feature.communication.websocket

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomWebSocketServer(
        port: Int? = null,
        private val onMessageReceived: (WebSocket?, String?) -> Unit
) : WebSocketServer(InetSocketAddress(port ?: PORT)) {

    private val connectedClientsSubject =
            BehaviorSubject.createDefault(0)

    init {
        isReuseAddr = true
        connectionLostTimeout = CONNECTION_LOST_TIMEOUT
    }

    private fun updateConnectedClients() {
        Timber.d("updateConnectedClients(): ${connections.size}")
        connectedClientsSubject.onNext(connections.size)
    }

    override fun onOpen(conn: WebSocket, handshake: ClientHandshake) {
        Timber.d("onOpen(${conn.remoteSocketAddress}, $handshake)")
        updateConnectedClients()
    }

    override fun onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
        Timber.d("onClose(${conn.remoteSocketAddress}, $code, $reason, $remote)")
        updateConnectedClients()
    }

    override fun onMessage(conn: WebSocket, message: String) {
        Timber.d("onMessage(${conn.remoteSocketAddress}, $message)")
        onMessageReceived(conn, message)
    }

    override fun onMessage(conn: WebSocket, message: ByteBuffer) {
        Timber.i("byte message")
        val buffer = ByteArray(message.remaining())
        message.get(buffer)
        onMessage(conn, String(buffer, Charset.defaultCharset()))
    }

    override fun onStart() {
        Timber.d("onStart()")
    }

    override fun onError(conn: WebSocket?, ex: Exception) {
        Timber.w("onError(${conn?.remoteSocketAddress}, $ex)")
    }

    fun connectedClients(): Observable<Int> =
            connectedClientsSubject

    fun startServer() = Completable.fromAction {
        start()
    }

    fun stopServer() = Completable.fromAction {
        stop()
    }

    companion object {
        internal const val PORT = 63124
        const val CONNECTION_LOST_TIMEOUT = 10
    }
}
