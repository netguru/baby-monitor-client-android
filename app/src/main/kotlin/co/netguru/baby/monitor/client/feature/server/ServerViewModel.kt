package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.extensions.toJson
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import co.netguru.baby.monitor.client.feature.websocket.Action
import co.netguru.baby.monitor.client.feature.websocket.CustomWebSocketServer
import co.netguru.baby.monitor.client.feature.websocket.LullabyCommand
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.BindException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServerViewModel @Inject constructor(
        private val nsdServiceManager: NsdServiceManager,
        private val lullabyPlayer: LullabyPlayer
) : ViewModel(), LullabyPlayer.PlaybackEvents {

    private val compositeDisposable = CompositeDisposable()
    private var webSocketServer: CustomWebSocketServer? = null
    private var openMessage = ""

    init {
        lullabyPlayer.playbackEvents = this
    }

    override fun onLullabyStarted(name: String, action: Action) {
        openMessage = LullabyCommand(name, action).toJson()
        webSocketServer?.openMessage = openMessage
        webSocketServer?.sendBroadcast(openMessage)
    }

    override fun onLullabyEnded(name: String, action: Action) {
        openMessage = LullabyCommand(name, action).toJson()
        webSocketServer?.openMessage = openMessage
        webSocketServer?.sendBroadcast(openMessage)
    }

    internal fun registerNsdService() {
        nsdServiceManager.registerService()
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun setupWebSocketServer() {
        webSocketServer = CustomWebSocketServer(
                onLullabyCommandReceived = { lullabyCommand ->
                    lullabyPlayer.handleActionRequest(lullabyCommand)
                            ?.subscribeOn(Schedulers.io())!!
                            .subscribeBy(
                                    onSuccess = { command ->
                                        webSocketServer?.sendBroadcast(command.toJson())
                                    },
                                    onError = Timber::e
                            )
                            .addTo(compositeDisposable)
                },
                onErrorListener = ::handleError
        ).also {
            it.runServer()
        }
    }

    internal fun stopWebSocketServer() {
        webSocketServer?.stopServer()
        webSocketServer?.onDestroy()
    }

    private fun handleError(exception: Exception) {
        //can turn off and on wifi to release ports
        webSocketServer?.onDestroy()
        if (exception is BindException) {
            Observable.timer(RETRY_TIMER, TimeUnit.SECONDS)
                    .map {
                        setupWebSocketServer()
                    }.subscribeOn(Schedulers.io())
                    .subscribeBy(onError = Timber::e)
                    .addTo(compositeDisposable)
        }
    }

    override fun onCleared() {
        super.onCleared()
        webSocketServer?.onDestroy()
        compositeDisposable.dispose()
        lullabyPlayer.clear()
    }

    companion object {
        private const val RETRY_TIMER = 30L
    }
}
