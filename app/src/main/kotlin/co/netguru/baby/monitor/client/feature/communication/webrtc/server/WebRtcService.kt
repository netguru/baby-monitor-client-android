package co.netguru.baby.monitor.client.feature.communication.webrtc.server

import android.app.Service
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.websocket.WebSocketServerService
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import org.java_websocket.WebSocket
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber

class WebRtcService : Service() {
    internal val webRtcManager = WebRtcManager(::sendMessage)

    private val disposables = CompositeDisposable()

    private lateinit var webSocketServerBinder: WebSocketServerService.Binder

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            when (service) {
                is WebSocketServerService.Binder -> {
                    webSocketServerBinder = service
                    webSocketServerBinder.messages()
                        .subscribeBy(onNext = ::handleMessage)
                        .addTo(disposables)
                }
                else ->
                    Timber.w("Unhandled service connected: $name, $service.")
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {}
    }

    override fun onBind(intent: Intent) =
        Binder()
            .also { Timber.d("onBind($intent)") }
            .also { webRtcManager.beginCapturing(this) }

    override fun onUnbind(intent: Intent): Boolean =
        super.onUnbind(intent)
            .also { Timber.d("onUnbind($intent)") }
            .also { webRtcManager.stopCapturing() }

    override fun onCreate() {
        Timber.i("onCreate()")
        AndroidInjection.inject(this)
        super.onCreate()
        bindService(
            Intent(applicationContext, WebSocketServerService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onDestroy() {
        unbindService(serviceConnection)
        disposables.clear()
        super.onDestroy()
    }

    private fun handleMessage(data: Pair<WebSocket, Message>) {
        Timber.i("handleMessage($data)")
        val (_, msg) = data
        msg.sdpOffer?.sdp?.let(webRtcManager::acceptOffer)
        msg.iceCandidate?.let(webRtcManager::addIceCandidate)
    }

    private fun sendMessage(message: Message) {
        webSocketServerBinder.sendMessage(message)
    }

    inner class Binder : android.os.Binder() {
        fun addSurfaceView(surfaceView: SurfaceViewRenderer) {
            webRtcManager.addSurfaceView(surfaceView)
        }
        fun enableCamera(enableCamera: Boolean) {
            webRtcManager.cameraEnabled = enableCamera
        }
        fun getConnectionObservable() = webRtcManager.getConnectionObservable()
    }
}
