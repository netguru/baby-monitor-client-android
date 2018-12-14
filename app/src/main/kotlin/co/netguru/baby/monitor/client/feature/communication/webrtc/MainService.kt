package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.app.Service
import android.content.Intent
import android.os.Binder
import co.netguru.baby.monitor.client.feature.common.extensions.let
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.P2P_OFFER
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.WEB_SOCKET_ACTION_RINGING
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketServer
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.WebSocket
import org.json.JSONObject
import timber.log.Timber
import kotlin.properties.Delegates

class MainService : Service() {

    private val compositeDisposable = CompositeDisposable()
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
        ).apply {
            runServer().subscribeOn(Schedulers.io()).subscribeBy(
                    onComplete = {
                        Timber.e("CustomWebSocketServer started")
                    },
                    onError = {
                        Timber.e("launch failed $it")
                        stopServer()
                    }
            ).addTo(compositeDisposable)
        }
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
        server?.let(::stopServer)
        compositeDisposable.dispose()
    }

    private fun stopServer(server: CustomWebSocketServer) {
        server.stopServer()
                .subscribeOn(Schedulers.io())
                .subscribeBy(
                        onComplete = {
                            Timber.e("CustomWebSocketServer closed")
                        },
                        onError = {
                            Timber.e("stop failed $it")
                        }
                ).addTo(compositeDisposable)
    }

    inner class MainBinder : Binder() {
        var callChangeNotifier: (RtcCall?) -> Unit = {}
        var currentCall by Delegates.observable<RtcCall?>(null) { _, _, newValue ->
            newValue?.let(callChangeNotifier)
        }

        fun createClient(client: CustomWebSocketClient) = RtcClient(client)

        fun cleanup() {
            currentCall?.cleanup()
            server?.let(::stopServer)
            callChangeNotifier = {}
        }

        fun handleBabyCrying() {
            server?.broadcast(
                    JSONObject().apply {
                        put(RtcCall.WEB_SOCKET_ACTION_KEY, RtcCall.BABY_IS_CRYING)
                        put("value", "") // iOS is expecting this empty field
                    }.toString().toByteArray()
            )
        }
    }

    companion object {
        const val SERVER_PORT = 10001
    }
}
