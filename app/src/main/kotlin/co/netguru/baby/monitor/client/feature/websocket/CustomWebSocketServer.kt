package co.netguru.baby.monitor.client.feature.websocket

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.Single
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
        port: Int? = null
) : WebSocketServer(InetSocketAddress(port ?: PORT)), LifecycleObserver {

    private val compositeDisposable = CompositeDisposable()
    private val connectionList = mutableListOf<WebSocket>()

    internal fun runServer() {
        Single.fromCallable {
            run()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onSuccess = {
                    Timber.i("CustomWebSocketServer started")
                },
                onError = {
                    Timber.e("launch failed $it")
                }
        ).addTo(compositeDisposable)
    }

    internal fun stopServer() {
        Single.fromCallable {
            stop()
        }.subscribeOn(Schedulers.io()).subscribeBy(
                onSuccess = {
                    Timber.i("CustomWebSocketServer closed")
                },
                onError = {
                    Timber.e("launch failed $it")
                }
        ).addTo(compositeDisposable)
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Timber.e("onOpen: ${conn?.remoteSocketAddress?.address?.hostAddress}")
        connectionList.add(conn ?: return)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Timber.i("onClose")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Timber.e(message)
    }

    override fun onStart() {
        Timber.i("CustomWebSocketServer started")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Timber.e("onError: $ex")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy() {
        compositeDisposable.dispose()
    }

    companion object {
        private const val PORT = 8887
    }
}
