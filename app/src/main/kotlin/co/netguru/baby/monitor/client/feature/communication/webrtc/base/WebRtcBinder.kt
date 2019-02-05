package co.netguru.baby.monitor.client.feature.communication.webrtc.base

import android.os.Binder

abstract class WebRtcBinder: Binder() {
    abstract fun cleanup()
}
