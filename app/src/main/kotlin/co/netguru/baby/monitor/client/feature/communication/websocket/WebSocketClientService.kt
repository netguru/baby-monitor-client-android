package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.java_websocket.client.WebSocketClient
import timber.log.Timber
import java.net.URI
import java.util.concurrent.TimeUnit

class WebSocketClientService : Service() {

    private var client: RxWebSocketClient? = null
    private val disposables = CompositeDisposable()

    override fun onDestroy() {
        disposables.clear()
    }

    private fun requireClient(serverUri: URI): RxWebSocketClient =
        client?.takeIf(WebSocketClient::isOpen)
            ?: RxWebSocketClient(serverUri = serverUri)
                .also { newClient -> client = newClient }
                .also { client ->
                    Timber.i("Connecting client to ${client.uri}.")
                    client.connect()
                }

    override fun onBind(intent: Intent?): IBinder? =
        Binder()

    inner class Binder : android.os.Binder() {
        fun events(serverUri: URI): Observable<RxWebSocketClient.Event> =
            requireClient(serverUri = serverUri).events().flatMap { event ->
                when (event) {
                    is RxWebSocketClient.Event.Close -> {
                        Timber.i("Received close event, expect restart.")
                        Observable.timer(3, TimeUnit.SECONDS)
                            .flatMap { events(serverUri = serverUri) }
                            .startWith(event)
                    }
                    else ->
                        Observable.just(event)
                }
            }

        fun send(message: String): Completable =
            Completable.fromAction {
                checkNotNull(client).run {
                    send(message)
                }
            }
    }
}
