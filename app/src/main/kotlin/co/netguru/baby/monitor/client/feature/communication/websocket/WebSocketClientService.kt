package co.netguru.baby.monitor.client.feature.communication.websocket

import android.app.Service
import android.content.Intent

class WebSocketClientService : Service() {

    private val client = RxWebSocketClient()

    override fun onBind(intent: Intent?): Binder =
        Binder()

    override fun onUnbind(intent: Intent?): Boolean {
        client.dispose()
        return super.onUnbind(intent)
    }

    inner class Binder : android.os.Binder() {
        fun client() =
            client
    }
}
