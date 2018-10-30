package co.netguru.baby.monitor.client.feature.websocket

import co.netguru.baby.monitor.client.common.extensions.toData
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

class CustomWebSocketServer(
        port: Int? = null,
        private val onLullabyCommandReceived: (LullabyCommand) -> Unit,
        private val onErrorListener: (Exception) -> Unit
) : WebSocketServer(InetSocketAddress(port ?: PORT)) {

    private val compositeDisposable = CompositeDisposable()
    internal var openMessage = ""

    internal fun runServer() {
        Completable.fromAction {
            run()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.i("CustomWebSocketServer started")
                },
                onError = {
                    Timber.e("launch failed $it")
                }
        ).addTo(compositeDisposable)
    }

    internal fun stopServer() {
        Completable.fromAction {
            stop()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onComplete = {
                    Timber.i("CustomWebSocketServer closed")
                },
                onError = {
                    Timber.e("stop failed $it")
                }
        ).addTo(compositeDisposable)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Timber.i("onOpen: ${conn?.remoteSocketAddress?.address?.hostAddress}")
        conn?.send(openMessage)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Timber.i(message)
        message?.toData<LullabyCommand>()?.let { command ->
            onLullabyCommandReceived(command)
        }
    }

    override fun onStart() {
        Timber.i("CustomWebSocketServer started")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Timber.e("onError: $ex")
        ex?.let(onErrorListener)
    }

    internal fun sendBroadcast(message: String) {
        Timber.i(message)
        broadcast(message)
    }

    internal fun onDestroy() {
        compositeDisposable.dispose()
    }

    companion object {
        internal const val PORT = 63124
    }
}
