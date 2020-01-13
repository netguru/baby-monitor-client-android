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
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClient
import co.netguru.baby.monitor.client.feature.communication.webrtc.client.RtcClientMessageController
import co.netguru.baby.monitor.client.feature.communication.websocket.MessageParser
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ClientLiveCameraFragmentViewModel @Inject constructor(
    private val messageParser: MessageParser,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private var rtcClient: RtcClient? = null
    val callInProgress = AtomicBoolean(false)
    private val compositeDisposable = CompositeDisposable()

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
        client: RxWebSocketClient
        ) {
        callInProgress.set(true)
        rtcClient = RtcClient(
            RtcClientMessageController(
                messageParser,
                serverUri,
                client
            ),
            this::handleStreamStateChange,
            liveCameraRemoteRenderer
        ).apply {
            startCall(context)
                .subscribeOn(Schedulers.newThread())
                .subscribeBy(
                    onComplete = {
                        Timber.i("Call started")
                    },
                    onError = {
                        endCall()
                        Timber.e(it, "Error during startCall")
                    }
                ).addTo(compositeDisposable)
        }
    }

    fun endCall() {
        compositeDisposable.clear()
        rtcClient?.cleanup()
        rtcClient = null
        callInProgress.set(false)
    }

    private fun handleStreamStateChange(streamState: StreamState) {
        when ((streamState as? ConnectionState)?.connectionState) {
            RtcConnectionState.Connected -> analyticsManager.logEvent(Event.Simple(EventType.VIDEO_STREAM_CONNECTED))
            RtcConnectionState.Error -> analyticsManager.logEvent(Event.Simple(EventType.VIDEO_STREAM_ERROR))
            else -> Unit
        }
        mutableStreamState.postValue(streamState)
    }
}
