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
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber

abstract class RtcCall(protected  val client: RxWebSocketClient) {

    var remoteView: CustomSurfaceViewRenderer? = null
    lateinit var offer: String

    protected val LOCAL_MEDIA_STREAM_LABEL = "stream1"
    protected val AUDIO_TRACK_ID = "audio1"
    protected val VIDEO_TRACK_ID = "video1"

    protected val VIDEO_WIDTH = 500
    protected val VIDEO_HEIGHT = 500
    protected val VIDEO_FPS = 30

    protected val compositeDisposable = CompositeDisposable()
    protected val eglBase by lazy { EglBase.create() }
    protected var sharedContext: EglBase.Context? = eglBase.eglBaseContext

    protected lateinit var constraints: MediaConstraints

    protected var callStateListener: (state: CallState) -> Unit = {}
    protected var streamStateListener: (streamState: StreamState) -> Unit = {}
    protected var state: CallState? = null
    protected var streamState: StreamState? = null

    protected var factory: PeerConnectionFactory? = null
    protected var connection: PeerConnection? = null

    protected var capturer: CameraVideoCapturer? = null
    protected var upStream: MediaStream? = null
    protected var dataChannel: DataChannel? = null
    protected var videoTrack: VideoTrack? = null
    protected var audioSource: AudioSource? = null
    protected var videoSource: VideoSource? = null
    protected var audio: AudioTrack? = null

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

    abstract fun createStream(): MediaStream?

    fun cleanup(
            clearSocket: Boolean = true,
            disposeConnection: Boolean = false
    ) {
        callStateListener = {}
        connection?.dispose()

        remoteView?.release()
        remoteView = null

        if (disposeConnection) {
            connection?.dispose()
        }

        audioSource?.dispose()
        capturer?.stopCapture()
        capturer?.dispose()

        videoTrack?.dispose()

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
        callStateListener(state)
    }

    protected fun reportStreamStateChange(state: StreamState) {
        this.streamState = state
        streamStateListener(state)
    }

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
                    Timber.e(e)
                }
            }
        }
    }

    protected fun handleDataChannel(dataChannel: DataChannel?) {
        this.dataChannel = dataChannel
        dataChannel?.registerObserver(dataChannelObserver)
    }

    protected fun createVideoTrack(isFacingFront: Boolean = false): VideoTrack? {
        capturer = createCapturer(isFacingFront)
        videoSource = factory?.createVideoSource(capturer)
        return factory?.createVideoTrack(VIDEO_TRACK_ID, videoSource)
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
        factory = PeerConnectionFactory(PeerConnectionFactory.Options())
        factory?.setVideoHwAccelerationOptions(sharedContext, sharedContext)
        Timber.i("created")
        constraints = MediaConstraints().apply {
            optional.add(MediaConstraints.KeyValuePair(HANDSHAKE_AUDIO_OFFER, "true"))
            optional.add(MediaConstraints.KeyValuePair(HANDSHAKE_VIDEO_OFFER, "true"))
            optional.add(MediaConstraints.KeyValuePair(HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT, "true"))
        }
    }

    private fun createCapturer(isFacingFront: Boolean = false): CameraVideoCapturer? {
        val enumerator = Camera1Enumerator()
        for (name in enumerator.deviceNames) {
            if (isFacingFront && enumerator.isFrontFacing(name)) {
                return enumerator.createCapturer(name, null)
            } else if (!isFacingFront && enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        return null
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

        internal const val WEB_SOCKET_ACTION_CALL = "call"
        internal const val WEB_SOCKET_ACTION_RINGING = "ringing"
        internal const val WEB_SOCKET_ACTION_CONNECTED = "connected"

        internal const val P2P_OFFER = "offerSDP"
        internal const val P2P_ANSWER = "answerSDP"

        internal const val BABY_IS_CRYING = "BABY_IS_CRYING"
        internal const val EVENT_RECEIVED_CONFIRMATION = "CRYING_EVENT_MESSAGE_RECEIVED"
        internal const val PUSH_NOTIFICATIONS_KEY = "PUSH_NOTIFICATIONS_KEY"

        private const val HANDSHAKE_AUDIO_OFFER = "OfferToReceiveAudio"
        private const val HANDSHAKE_VIDEO_OFFER = "OfferToReceiveVideo"

        private const val HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT = "DtlsSrtpKeyAgreement"
        private const val STATE_CHANGE_MESSAGE = "StateChange"
    }
}
