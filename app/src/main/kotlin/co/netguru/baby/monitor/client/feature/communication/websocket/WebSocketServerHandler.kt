package co.netguru.baby.monitor.client.feature.communication.websocket

import android.arch.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.communication.SERVER_PORT
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.WebSocket
import timber.log.Timber
import java.util.concurrent.TimeUnit

class WebSocketServerHandler(
        private val handleMessage: (WebSocket?, String?) -> Unit
) {
    private val clientConnectionStatus = MutableLiveData<ClientConnectionStatus>()

    fun clientConnectionStatus() =
            clientConnectionStatus

    private val compositeDisposable = CompositeDisposable()
    private var server: CustomWebSocketServer? = null

    fun startServer() {
        if (server != null) return

        compositeDisposable.clear()
        server = CustomWebSocketServer(SERVER_PORT,
                onMessageReceived = { webSocket, message -> handleMessage(webSocket, message) }
        ).apply {
            startServer().subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = {
                                Timber.i("CustomWebSocketServer started")
                            },
                            onError = { e ->
                                restartServer()
                                Timber.e("launch failed $e")
                            }
                    ).addTo(compositeDisposable)
            connectedClients()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(onNext = { connections ->
                        clientConnectionStatus.postValue(
                                if (connections > 0) ClientConnectionStatus.CLIENT_CONNECTED
                                else ClientConnectionStatus.EMPTY)
                    })
                    .addTo(compositeDisposable)
        }
    }

    fun restartServer() {
        stopServer()
        Completable.timer(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            startServer()
                        },
                        onError = { e ->
                            Timber.e(e)
                            restartServer()
                        }
                ).addTo(compositeDisposable)
    }

    fun stopServer(shouldRestart: Boolean = false) {
        compositeDisposable.clear()
        server?.let { server -> stopServer(shouldRestart, server) }
        server = null
    }

    fun broadcast(byteArray: ByteArray) {
        server?.broadcast(byteArray)
    }

    private fun stopServer(shouldRestart: Boolean, server: CustomWebSocketServer) {
        server.stopServer()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            Timber.i("CustomWebSocketServer closed")
                            if (shouldRestart) {
                                startServer()
                            }
                        },
                        onError = {
                            Timber.e("stop failed $it")
                            stopServer(shouldRestart)
                        }
                ).addTo(compositeDisposable)
    }
}
