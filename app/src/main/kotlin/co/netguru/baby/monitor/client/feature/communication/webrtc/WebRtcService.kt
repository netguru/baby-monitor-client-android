package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.app.Service
import android.content.Intent
import dagger.android.AndroidInjection
import org.webrtc.SurfaceViewRenderer
import timber.log.Timber
import javax.inject.Inject

class WebRtcService : Service() {
    @Inject
    internal lateinit var webRtcManager: WebRtcManager

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
    }

    inner class Binder : android.os.Binder() {
        fun addSurfaceView(surfaceView: SurfaceViewRenderer) {
            webRtcManager.addSurfaceView(surfaceView)
        }
    }
}
