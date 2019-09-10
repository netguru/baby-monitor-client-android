package co.netguru.baby.monitor.client.feature.communication.websocket

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import timber.log.Timber
import java.net.URI

class RxWebSocketClient(serverUri: URI) : WebSocketClient(serverUri) {

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
    }
}
