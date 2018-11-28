package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import co.netguru.baby.monitor.client.feature.common.extensions.let
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.P2P_OFFER
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.WEB_SOCKET_ACTION_RINGING
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketServer
import org.java_websocket.WebSocket
import org.json.JSONObject
import timber.log.Timber
import kotlin.properties.Delegates

class MainService : Service() {

    private var server: CustomWebSocketServer? = null
    private lateinit var mainBinder: MainBinder

    override fun onCreate() {
        super.onCreate()
        initNetwork()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY


    override fun onBind(intent: Intent?) = MainBinder().also { mainBinder = it }

    private fun initNetwork() {
        server = CustomWebSocketServer(SERVER_PORT,
                onConnectionRequestReceived = { webSocket, message ->
                    (webSocket to message).let(this::handleClient)
                },
                onErrorListener = Timber::e
        )
    }

    private fun handleClient(client: WebSocket, message: String) {
        val jsonObject = JSONObject(message)
        if (jsonObject.has(P2P_OFFER)) {
            Timber.i("$WEB_SOCKET_ACTION_RINGING...")
            mainBinder.currentCall = RtcReceiver(
                    client,
                    jsonObject.getJSONObject(P2P_OFFER).getString("sdp")
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainBinder.cleanup()
        server?.onDestroy()
    }

    inner class MainBinder : Binder() {
        var callChangeNotifier: (RtcCall?) -> Unit = {}
        var currentCall by Delegates.observable<RtcCall?>(null) { _, _, newValue ->
            newValue?.let(callChangeNotifier)
        }

        fun createClient(address: String) = RtcClient(address)

        fun cleanup() {
            currentCall?.cleanup()
            server?.onDestroy()
            callChangeNotifier = {}
        }
    }

    companion object {
        const val SERVER_PORT = 10001
    }
}
