package co.netguru.baby.monitor.client.feature.client.home.livecamera

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.feature.analytics.AnalyticsManager
import co.netguru.baby.monitor.client.feature.analytics.Event
import co.netguru.baby.monitor.client.feature.analytics.EventType
import co.netguru.baby.monitor.client.feature.communication.webrtc.ConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.RtcConnectionState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClientController
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ClientLiveCameraFragmentViewModel @Inject constructor(
    private val analyticsManager: AnalyticsManager,
    private val rtcClientController: RtcClientController
) : ViewModel() {

    val callInProgress = AtomicBoolean(false)

    private val mutableStreamState = MutableLiveData<StreamState>()
    val streamState: LiveData<StreamState> = mutableStreamState

    override fun onCleared() {
        super.onCleared()
        endCall()
    }

    fun startCall(
        context: Context,
        liveCameraRemoteRenderer: CustomSurfaceViewRenderer,
        serverUri: URI,
        client: RxWebSocketClient,
        hasRecordAudioPermission: Boolean
    ) {
        rtcClientController.startCall(
            context,
            liveCameraRemoteRenderer,
            serverUri,
            client,
            this::handleStreamStateChange,
            hasRecordAudioPermission
        )
        callInProgress.set(true)
    }

    fun pushToSpeak(isEnabled: Boolean) {
        rtcClientController.pushToSpeak(isEnabled)
    }

    fun endCall() {
        rtcClientController.endCall()
        callInProgress.set(false)
    }

    private fun handleStreamStateChange(streamState: StreamState) {
        when ((streamState as? ConnectionState)?.connectionState) {
            RtcConnectionState.Connected -> analyticsManager.logEvent(Event.Simple(EventType.VIDEO_STREAM_CONNECTED))
            RtcConnectionState.Error -> analyticsManager.logEvent(Event.Simple(EventType.VIDEO_STREAM_ERROR))
            else -> Unit
        }
        mutableStreamState.value = streamState
    }
}
