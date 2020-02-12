package co.netguru.baby.monitor.client.feature.communication.websocket

import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketServer.Companion.CONNECTION_LOST_TIMEOUT
import com.google.gson.Gson
import dagger.Reusable
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class RxWebSocketClient @Inject constructor(
    private val gson: Gson
) {

    private var client: RxWebSocketClient? = null

    private fun requireActiveClient(serverUri: URI) =
        client?.takeUnless { it.isClosing || it.isClosed }
            ?: RxWebSocketClient(serverUri = serverUri)
                .also { newClient -> client = newClient }
                .also { client ->
                    if (client.uri != serverUri || !client.isOpen) {
                        Timber.i("Connecting client to ${client.uri}.")
                        client.connect()
                    }
                }

    fun events(serverUri: URI): Observable<Event> =
        requireActiveClient(serverUri = serverUri)
            .events()
            .startWith(if (isOpen()) Event.Connected else Event.Disconnected)
            .flatMap { event ->
                if (event is Event.Close) {
                    Timber.i("Received close event, expect restart.")
                    Observable.timer(CLOSE_EVENT_RESTART_DELAY, TimeUnit.SECONDS)
                        .flatMap { events(serverUri = serverUri) }
                        .startWith(event)
                } else {
                    Observable.just(event)
                }
            }

    fun events() = client?.events()

    fun send(message: Message): Completable =
        Completable.fromAction {
            checkNotNull(client).run {
                send(gson.toJson(message))
            }
        }

    private fun isOpen() = client?.isOpen == true

    fun dispose() {
        client?.close()
        client = null
    }

    companion object {
        private const val CLOSE_EVENT_RESTART_DELAY = 3L
    }

    private class RxWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {

        init {
            connectionLostTimeout = CONNECTION_LOST_TIMEOUT
        }

        private val events: Subject<Event> = PublishSubject.create()

        override fun onOpen(handshakedata: ServerHandshake) {
            Timber.d("onOpen($handshakedata)")
            events.onNext(Event.Open)
        }

        override fun onClose(code: Int, reason: String, remote: Boolean) {
            Timber.d("onClose($code, $reason, $remote)")
            events.onNext(Event.Close(code = code, reason = reason, remote = remote))
            events.onComplete()
        }

        override fun onMessage(message: String) {
            Timber.d("onMessage($message)")
            events.onNext(Event.Message(message = message))
        }

        override fun onError(ex: Exception) {
            Timber.d("onError($ex)")
            events.onNext(Event.Error(error = ex))
        }

        fun events(): Observable<Event> =
            events
    }

    sealed class Event {
        /**
         * @see [WebSocketClient.onOpen]
         */
        object Open : Event()

        /**
         * @see [WebSocketClient.onClose]
         */
        data class Close(val code: Int, val reason: String, val remote: Boolean) : Event()

        /**
         * @see [WebSocketClient.onMessage]
         */
        data class Message(val message: String) : Event()

        /**
         * @see [WebSocketClient.onError]
         */
        data class Error(val error: Throwable) : Event()

        object Connected : Event()

        object Disconnected : Event()
    }
}
