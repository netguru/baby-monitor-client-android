package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.Service
import android.content.Intent
import com.google.gson.Gson
import com.google.gson.JsonParseException
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.java_websocket.WebSocket
import timber.log.Timber
import javax.inject.Inject

class WebSocketServerService : Service() {

    private lateinit var serverHandler: WebSocketServerHandler
    @Inject
    internal lateinit var gson: Gson

    private val messages = PublishSubject.create<Pair<WebSocket, Message>>()

    override fun onBind(intent: Intent) =
        Binder()

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        serverHandler = WebSocketServerHandler { ws, msg ->
            Timber.d("Got a message: ${msg?.substring(0, 20)}.")
            val message = try {
                gson.fromJson(msg, Message::class.java)
            } catch (e: JsonParseException) {
                Timber.w(e)
                null
            }
            Timber.d("Serialized message: $message.")
            if (ws != null && message != null)
                messages.onNext(ws to message)
        }
            .apply { startServer() }
    }

    override fun onDestroy() {
        serverHandler.stopServer()
        super.onDestroy()
    }

    inner class Binder : android.os.Binder() {
        fun sendMessage(message: Message) {
            Timber.d("sendMessage($message)")
            serverHandler.broadcast(message.let(gson::toJson).toByteArray())
        }

        /**
         * @return a stream of incoming [Message]s.
         */
        fun messages(): Observable<Pair<WebSocket, Message>> =
            messages

        fun clientConnectionStatus() =
            serverHandler.clientConnectionStatus()
    }
}
