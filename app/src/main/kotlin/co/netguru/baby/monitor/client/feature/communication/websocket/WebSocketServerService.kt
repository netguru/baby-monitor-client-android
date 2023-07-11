package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.Service
import android.content.Intent
import co.netguru.baby.monitor.client.application.di.AppComponent.Companion.appComponent
import co.netguru.baby.monitor.client.data.DataRepository
import co.netguru.baby.monitor.client.data.communication.websocket.ClientConnectionStatus
import co.netguru.baby.monitor.client.feature.debug.DebugNotificationManager
import com.google.gson.Gson
import com.google.gson.JsonParseException
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.java_websocket.WebSocket
import timber.log.Timber
import javax.inject.Inject

class WebSocketServerService : Service() {

    private lateinit var serverHandler: WebSocketServerHandler
    @Inject
    internal lateinit var gson: Gson
    @Inject
    internal lateinit var debugNotificationManager: DebugNotificationManager

    private val messages = PublishSubject.create<Pair<WebSocket, Message>>()

    @Inject
    internal lateinit var dataRepo: DataRepository

    override fun onBind(intent: Intent) =
        Binder()

    override fun onCreate() {
        super.onCreate()

        appComponent.inject(this)
        serverHandler = WebSocketServerHandler { ws, msg ->
            val message = try {
                gson.fromJson(msg, Message::class.java)
            } catch (e: JsonParseException) {
                Timber.w(e)
                null
            }
            Timber.d("Serialized message: $message.")
            if (ws != null && message != null) {
                messages.onNext(ws to message)
            }
        }
            .apply { startServer() }
        debugNotificationManager.show(this)
    }

    override fun onDestroy() {
        serverHandler.stopServer()
        debugNotificationManager.clear(this)
        super.onDestroy()
    }

    inner class Binder : android.os.Binder() {
        fun sendMessage(message: Message) {
            Timber.d("sendMessage($message)")
            serverHandler.broadcast(message.let(gson::toJson))
        }

        /**
         * @return a stream of incoming [Message]s.
         */
        fun messages(): Observable<Pair<WebSocket, Message>> =
            messages

        fun clientConnectionStatus(): Observable<ClientConnectionStatus> =
            serverHandler.clientConnectionStatus
    }
}
