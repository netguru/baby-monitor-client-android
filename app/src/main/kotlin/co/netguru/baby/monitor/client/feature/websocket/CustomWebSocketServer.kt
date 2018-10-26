package co.netguru.baby.monitor.client.feature.websocket

import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
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
        private val onLullabyRequestReceived: (String) -> Unit,
        private val onErrorListener: (Exception) -> Unit
) : WebSocketServer(InetSocketAddress(port ?: PORT)) {

    private val compositeDisposable = CompositeDisposable()
    private val connectionList = mutableListOf<WebSocket>()

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
        connectionList.add(conn ?: return)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Timber.i(message)
        LullabyPlayer.lullabies.find { it.name == message }?.let { lullaby ->
            onLullabyRequestReceived(lullaby.name)
        }
    }

    override fun onStart() {
        Timber.i("CustomWebSocketServer started")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Timber.e("onError: $ex")
        ex?.let(onErrorListener)
    }

    internal fun onDestroy() {
        compositeDisposable.dispose()
    }

    companion object {
        internal const val PORT = 63124
    }
}
