package co.netguru.baby.monitor.client.feature.communication.websocket

import android.arch.lifecycle.MutableLiveData
import co.netguru.baby.monitor.client.data.communication.websocket.ServerStatus
import co.netguru.baby.monitor.client.feature.communication.webrtc.receiver.WebRtcReceiverService
import io.reactivex.Completable
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
    internal val serverStatus = MutableLiveData<ServerStatus>()

    private val compositeDisposable = CompositeDisposable()
    private var server: CustomWebSocketServer? = null

    fun startServer() {
        if (serverStatus.value == ServerStatus.STARTED) {
            return
        }

        compositeDisposable.clear()
        server = CustomWebSocketServer(WebRtcReceiverService.SERVER_PORT,
                onMessageReceived = { webSocket, message -> handleMessage(webSocket, message) },
                onConnectionStatusChange = this::onConnectionStatusChange
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
        }
    }

    fun restartServer() {
        compositeDisposable.clear()
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
    }

    fun broadcast(byteArray: ByteArray) {
        server?.broadcast(byteArray)
    }

    private fun onConnectionStatusChange(status: ServerStatus) {
        serverStatus.postValue(status)
        if (serverStatus == ServerStatus.ERROR) {
            restartServer()
        }
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
