package co.netguru.baby.monitor.client.feature.communication.websocket

import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import timber.log.Timber
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.charset.Charset

class CustomWebSocketServer(
        port: Int? = null,
        private val onConnectionRequestReceived: (WebSocket?, String?) -> Unit,
        private val onErrorListener: (Exception) -> Unit
) : WebSocketServer(InetSocketAddress(port ?: PORT)) {

    private val compositeDisposable = CompositeDisposable()

    init {
        isReuseAddr = true
        runServer()
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Timber.i("onOpen: ${conn?.remoteSocketAddress?.address?.hostAddress}")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Timber.i(message)
        onConnectionRequestReceived(conn, message)
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

    fun onDestroy() {
        compositeDisposable.dispose()
        stopServer()
    }

    private fun runServer() {
        Completable.fromAction {
            start()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.e("CustomWebSocketServer started")
                },
                onError = {
                    Timber.e("launch failed $it")
                    stopServer()
                }
        ).addTo(compositeDisposable)
    }

    private fun stopServer() {
        Timber.e("stopServer")
        Completable.fromAction {
            stop(TIMEOUT)
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.e("CustomWebSocketServer closed")
                },
                onError = {
                    Timber.e("stop failed $it")
                }
        ).addTo(compositeDisposable)
    }

    companion object {
        internal const val PORT = 63124
        private const val TIMEOUT = -1
    }
}
