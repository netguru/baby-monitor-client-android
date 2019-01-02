package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.app.Service
import android.content.Intent
import android.os.Binder
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.P2P_OFFER
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcCall.Companion.WEB_SOCKET_ACTION_RINGING
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketClient
import co.netguru.baby.monitor.client.feature.communication.websocket.CustomWebSocketServer
import io.reactivex.Maybe
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.java_websocket.WebSocket
import org.json.JSONObject
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber

class WebRtcService : Service() {

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
                    if (webSocket != null && message != null) {
                        handleClient(webSocket, message)
                    }
                },
                onErrorListener = Timber::e
        ).apply {
            startServer().subscribeOn(Schedulers.io()).subscribeBy(
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
            mainBinder.currentCall?.let { call ->
                if (call is RtcReceiver) {
                    call.accept(
                            client,
                            jsonObject.getJSONObject(P2P_OFFER).getString("sdp")
                    ).subscribeOn(Schedulers.newThread())
                            .subscribeBy(
                                    onComplete = { Timber.i("completed") },
                                    onError = Timber::e
                            ).addTo(compositeDisposable)
                }
            }
        }
    }

    override fun onDestroy() {
        mainBinder.cleanup()
        server?.let(::stopServer)
        compositeDisposable.dispose()
        super.onDestroy()
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
        var currentCall: RtcCall? = null

        fun createClient(client: CustomWebSocketClient) = RtcClient(client)

        fun createReceiver(
                view: SurfaceViewRenderer,
                listener: (state: CallState) -> Unit
        ) {
            currentCall = RtcReceiver(applicationContext, view, listener)
        }

        fun hangUpReceiver() = Maybe.just(currentCall)
                .flatMapCompletable { call ->
                    server?.let(this@WebRtcService::stopServer)
                    initNetwork()
                    (call as RtcReceiver).stopCall()
                }


        fun cleanup() {
            Timber.i("cleanup")
            currentCall?.let(this::callCleanup)
            server?.stop()
        }

        fun handleBabyCrying() {
            server?.broadcast(
                    JSONObject().apply {
                        put(RtcCall.WEB_SOCKET_ACTION_KEY, RtcCall.BABY_IS_CRYING)
                        put("value", "") // iOS is expecting this empty field
                    }.toString().toByteArray()
            )
        }

        private fun callCleanup(call: RtcCall) {
            call.cleanup(false)
                    .subscribeOn(Schedulers.io())
                    .subscribeBy(
                            onComplete = {
                                server?.let(this@WebRtcService::stopServer)
                            },
                            onError = Timber::e
                    ).addTo(compositeDisposable)
        }

        fun stopServer() {
            server?.let(this@WebRtcService::stopServer)
        }
    }

    companion object {
        const val SERVER_PORT = 10001
    }
}