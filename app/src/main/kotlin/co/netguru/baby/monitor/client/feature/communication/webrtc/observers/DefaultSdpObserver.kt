package co.netguru.baby.monitor.client.feature.communication.webrtc.observers

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription

open class DefaultSdpObserver(
        private val onSetFailure: (error: String?) -> Unit = {},
        private val onSetSuccess: () -> Unit = {},
        private val onCreateSuccess: (sessionDescription: SessionDescription?) -> Unit = {},
        private val onCreateFailure: (error: String?) -> Unit = {}
) : SdpObserver {

    override fun onSetFailure(error: String?) {
        onSetFailure.invoke(error)
    }

    override fun onSetSuccess() {
        onSetSuccess.invoke()
    }
    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
        onCreateSuccess.invoke(sessionDescription)
    }
    override fun onCreateFailure(error: String?){
        onCreateFailure.invoke(error)
    }
}
