package co.netguru.baby.monitor.client.feature.communication.webrtc.base

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.data.communication.webrtc.CallState
import co.netguru.baby.monitor.client.feature.communication.webrtc.StreamState
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.RxWebSocketClient
import com.crashlytics.android.Crashlytics
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber

abstract class RtcCall(protected val client: RxWebSocketClient) {

    var remoteView: CustomSurfaceViewRenderer? = null

    protected val compositeDisposable = CompositeDisposable()
    private val eglBase by lazy { EglBase.create() }
    private var sharedContext: EglBase.Context? = eglBase.eglBaseContext

    protected lateinit var constraints: MediaConstraints

    protected var callStateListener: ((state: CallState) -> Unit)? = null
    protected var streamStateListener: ((streamState: StreamState) -> Unit)? = null
    protected var state: CallState? = null
    private var streamState: StreamState? = null

    protected var factory: PeerConnectionFactory? = null
    protected var connection: PeerConnection? = null

    protected var dataChannel: DataChannel? = null

    val dataChannelObserver = object : DataChannel.Observer {
        override fun onMessage(buffer: DataChannel.Buffer?) {
            buffer ?: return
            val data = ByteArray(buffer.data.remaining())
            buffer.data.get(data)
            handleMessage(JSONObject(String(data)))
        }

        override fun onBufferedAmountChange(l: Long) = Unit

        override fun onStateChange() = Unit
    }

    fun cleanup() {
        callStateListener = null
        streamStateListener = null
        connection?.dispose()
        dataChannel?.unregisterObserver()

        remoteView?.release()
        remoteView = null

        compositeDisposable.dispose()
    }

    protected fun handleAnswer(remoteDesc: String) {
        connection?.setRemoteDescription(
            DefaultSdpObserver(
                onSetSuccess = { Timber.i("Set success") },
                onSetFailure = { message -> Timber.e("Failure: $message") }
            ),
            SessionDescription(SessionDescription.Type.ANSWER, remoteDesc)
        )
    }

    protected fun reportStateChange(state: CallState) {
        this.state = state
        callStateListener?.invoke(state)
    }

    protected fun reportStreamStateChange(state: StreamState) {
        this.streamState = state
        streamStateListener?.invoke(state)
    }

    @Suppress("TooGenericExceptionCaught")
    protected fun handleMediaStream(mediaStream: MediaStream) {
        Handler(Looper.getMainLooper()).post {
            remoteView?.let { view ->
                try {
                    view.setBackgroundColor(Color.TRANSPARENT)
                    if (!view.initialized) {
                        view.init(sharedContext, null)
                    }
                    if (mediaStream.videoTracks.size > 0) {
                        mediaStream.videoTracks[0].addSink(view)
                    }
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
            }
        }
    }

    protected fun handleDataChannel(dataChannel: DataChannel?) {
        this.dataChannel = dataChannel
        dataChannel?.registerObserver(dataChannelObserver)
    }

    protected fun initRtc(context: Context) {
        Timber.i("initializing")
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions()
        )
        Timber.i("initialized")
        factory = PeerConnectionFactory.builder()
            .createPeerConnectionFactory()
            .apply {
                setVideoHwAccelerationOptions(sharedContext, sharedContext)
            }
        Timber.i("created")
        constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_AUDIO_OFFER, "true"))
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_VIDEO_OFFER, "true"))
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT, "true"))
        }
    }

    private fun handleMessage(jsonObject: JSONObject) {
        if (jsonObject.has(STATE_CHANGE_MESSAGE)) {
            Single.fromCallable {
                val state = jsonObject.getString(STATE_CHANGE_MESSAGE)
                this@RtcCall.remoteView?.setBackgroundColor(Color.TRANSPARENT)
                state
            }.subscribeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onSuccess = { Timber.i("Success $it") }
                )
        }
    }

    companion object {
        internal const val WEB_SOCKET_ACTION_KEY = "action"

        internal const val P2P_OFFER = "offerSDP"
        internal const val P2P_ANSWER = "answerSDP"

        internal const val PUSH_NOTIFICATIONS_KEY = "PUSH_NOTIFICATIONS_KEY"

        private const val HANDSHAKE_AUDIO_OFFER = "OfferToReceiveAudio"
        private const val HANDSHAKE_VIDEO_OFFER = "OfferToReceiveVideo"

        private const val HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT = "DtlsSrtpKeyAgreement"
        private const val STATE_CHANGE_MESSAGE = "StateChange"
    }
}
