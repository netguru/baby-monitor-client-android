package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.app.Service
import android.content.Intent
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerHandler
import dagger.android.AndroidInjection
import org.java_websocket.WebSocket
import org.json.JSONObject
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber

class WebRtcService : Service() {
    internal val webRtcManager = WebRtcManager(::sendMessage)

    private val webSocketServerHandler = WebSocketServerHandler(::handleWebSocketMessage)

    override fun onBind(intent: Intent) =
        Binder()
            .also { Timber.d("onBind($intent)") }
            .also { webSocketServerHandler.startServer() }
            .also { webRtcManager.beginCapturing(this) }

    override fun onUnbind(intent: Intent): Boolean =
        super.onUnbind(intent)
            .also { Timber.d("onUnbind($intent)") }
            .also { webSocketServerHandler.stopServer() }
            .also { webRtcManager.stopCapturing() }

    override fun onCreate() {
        Timber.i("onCreate()")
        AndroidInjection.inject(this)
        super.onCreate()
    }

    private fun handleWebSocketMessage(webSocket: WebSocket?, message: String?) {
        Timber.d("handleWebSocketMessage($webSocket, $message)")
        message?.let(::JSONObject)
            ?.optJSONObject("offerSDP")
            ?.optString("sdp")
            ?.let { offer -> webRtcManager.acceptOffer(offer) }
    }

    private fun sendMessage(message: String) {
        webSocketServerHandler.broadcast(message.toByteArray())
    }

    inner class Binder : android.os.Binder() {
        fun clientConnectionStatus() =
            webSocketServerHandler.clientConnectionStatus()

        fun addSurfaceView(surfaceView: SurfaceViewRenderer) {
            webRtcManager.addSurfaceView(surfaceView)
        }
    }
}
