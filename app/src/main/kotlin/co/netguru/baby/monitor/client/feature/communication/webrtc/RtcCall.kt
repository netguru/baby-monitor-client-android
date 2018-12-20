package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import org.java_websocket.WebSocket
import org.json.JSONObject
import org.webrtc.*
import timber.log.Timber

abstract class RtcCall {

    var remoteRenderer: SurfaceViewRenderer? = null

    protected val LOCAL_MEDIA_STREAM_LABEL = "stream1"
    protected val AUDIO_TRACK_ID = "audio1"
    protected val compositeDisposable = CompositeDisposable()
    protected val eglBase by lazy { EglBase.create() }
    protected val sharedContext: EglBase.Context by lazy { eglBase.eglBaseContext }

    protected lateinit var constraints: MediaConstraints
    protected lateinit var offer: String

    protected var listener: (state: CallState) -> Unit = {}
    protected var state: CallState? = null
    protected var commSocket: WebSocket? = null

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

    fun hangUp(): Completable = Completable.fromAction {
        if (commSocket?.isOpen == true) {
            commSocket?.send(
                    JSONObject().apply {
                        put(WEB_SOCKET_ACTION_KEY, "dismissed")
                    }.toString()
            )
            commSocket?.close()
            connection?.close()
        }
    }

    fun cleanup(
            clearSocket: Boolean = true,
            disposeConnection: Boolean = false
    ): Completable = Completable.fromAction {
        listener = {}
        connection?.close()
        if (disposeConnection) {
            connection?.dispose()
        }
        eglBase.release()
        audioSource?.dispose()
        capturer?.stopCapture()
        capturer?.dispose()

        if (clearSocket) {
            commSocket?.close()
        }
        remoteRenderer?.release()
        remoteRenderer = null
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
        listener(state)
    }

    protected fun handleMediaStream(mediaStream: MediaStream) {
        Handler(Looper.getMainLooper()).post {
            remoteRenderer?.let { renderer ->
                try {
                    renderer.setBackgroundColor(Color.TRANSPARENT)
                    renderer.init(sharedContext, null)
                    if (mediaStream.videoTracks.size > 0) {
                        mediaStream.videoTracks[0].addSink(renderer)
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
    }

    protected fun createVideoTrack(): VideoTrack? {
        capturer = createCapturer()
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

    private fun createCapturer(): CameraVideoCapturer? {
        val enumerator = Camera1Enumerator()
        for (name in enumerator.deviceNames) {
            if (enumerator.isBackFacing(name)) {
                return enumerator.createCapturer(name, null)
            }
        }
        return null
    }

    private fun handleMessage(jsonObject: JSONObject) {
        if (jsonObject.has(STATE_CHANGE_MESSAGE)) {
            Single.fromCallable {
                val state = jsonObject.getString(STATE_CHANGE_MESSAGE)
                this@RtcCall.remoteRenderer?.setBackgroundColor(Color.TRANSPARENT)
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

        internal const val BABY_IS_CRYING = "BABY_IS_CRYING"

        internal const val P2P_OFFER = "offerSDP"
        internal const val P2P_ANSWER = "answerSDP"

        private const val HANDSHAKE_AUDIO_OFFER = "OfferToReceiveAudio"
        private const val HANDSHAKE_VIDEO_OFFER = "OfferToReceiveVideo"
        private const val HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT = "DtlsSrtpKeyAgreement"

        private const val STATE_CHANGE_MESSAGE = "StateChange"
        private const val VIDEO_TRACK_ID = "video1"
    }
}
