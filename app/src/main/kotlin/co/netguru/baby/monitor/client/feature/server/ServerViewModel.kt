package co.netguru.baby.monitor.client.feature.server

import android.arch.lifecycle.ViewModel
import co.netguru.baby.monitor.client.data.server.NsdServiceManager
import co.netguru.baby.monitor.client.feature.server.player.LullabyPlayer
import co.netguru.baby.monitor.client.feature.websocket.CustomWebSocketServer
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
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()
    private var webSocketServer: CustomWebSocketServer? = null

    internal fun registerNsdService() {
        nsdServiceManager.registerService()
    }

    internal fun unregisterNsdService() {
        nsdServiceManager.unregisterService()
    }

    internal fun setupWebSocketServer() {
        webSocketServer = CustomWebSocketServer(
                onLullabyRequestReceived = { title ->
                    lullabyPlayer.play(title)
                            ?.subscribeOn(Schedulers.io())!!
                            .subscribeBy(
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
