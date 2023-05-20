package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.feature.communication.webrtc.*
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcMessageHandler
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.ConnectionObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import org.webrtc.*
import timber.log.Timber

class RtcClient(
    private val rtcClientMessageController: RtcClientMessageController,
    private val streamStateListener: (streamState: StreamState) -> Unit,
    private val remoteView: CustomSurfaceViewRenderer
) : RtcMessageHandler {

    private var audioTrack: AudioTrack? = null
    private var remoteAudioTrack: AudioTrack? = null
    private var audioSource: AudioSource? = null
    private val compositeDisposable = CompositeDisposable()
    private val eglBase by lazy { EglBase.create() }
    private var sharedContext: EglBase.Context? = eglBase.eglBaseContext

    private lateinit var constraints: MediaConstraints
    private lateinit var connectionObserver: ConnectionObserver

    private var isCallConnected = false

    private var peerConnection: PeerConnection? = null

    var microphoneEnabled = false
        set(isEnabled) {
            field = isEnabled
            audioTrack?.setEnabled(isEnabled)
            remoteAudioTrack?.setEnabled(!isEnabled)
        }

    init {
        rtcClientMessageController.rtcMessageHandler = this
    }

    fun startCall(
        context: Context,
        hasRecordAudioPermission: Boolean
    ) = Completable.fromAction {
        initRtc(context, hasRecordAudioPermission)
    }

    fun cleanup() {
        peerConnection?.dispose()
        rtcClientMessageController.dispose()
        remoteView.release()
        compositeDisposable.dispose()
    }

    private fun reportStreamStateChange(state: StreamState) {
        streamStateListener.invoke(state)
    }

    @Suppress("TooGenericExceptionCaught")
    private fun handleMediaStream(mediaStream: MediaStream) {
        Handler(Looper.getMainLooper()).post {
            remoteView.let { view ->
                try {
                    view.setBackgroundColor(Color.TRANSPARENT)
                    if (!view.initialized) {
                        view.init(sharedContext, null)
                    }
                    if (mediaStream.videoTracks.size > 0) {
                        mediaStream.videoTracks[0].addSink(view)
                    }
                    if (mediaStream.audioTracks.size > 0) {
                        remoteAudioTrack = mediaStream.audioTracks[0]
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }
        }
    }

    private fun initRtc(
        context: Context,
        hasRecordAudioPermission: Boolean
    ) {
        Timber.i("initializing")
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        Timber.i("initialized")
        val encoderFactory = DefaultVideoEncoderFactory(sharedContext, true, false)
        val decoderFactory = DefaultVideoDecoderFactory(sharedContext)
        val factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
        Timber.i("created")
        constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_AUDIO_OFFER, "true"))
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_VIDEO_OFFER, "true"))
            mandatory.add(MediaConstraints.KeyValuePair(HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT, "true"))
        }

        setSpeakerphoneOn(context)

        createConnection(factory ?: return, hasRecordAudioPermission)
    }

    private fun setSpeakerphoneOn(context: Context) {
        (context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager)?.run {
            isSpeakerphoneOn = true
        }
    }

    private fun createConnection(
        factory: PeerConnectionFactory,
        hasRecordAudioPermission: Boolean
    ) {
        connectionObserver = ConnectionObserver()

        peerConnection = factory.createPeerConnection(
            emptyList(),
            connectionObserver
        )
        if (hasRecordAudioPermission) addAudioTrack(factory)

        connectionObserver.streamObservable
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { streamState ->
                when (streamState) {
                    is OnIceCandidatesChange ->
                        rtcClientMessageController.handleIceCandidateChange(streamState.iceCandidateState)
                    is OnAddStream ->
                        handleMediaStream(streamState.mediaStream)
                    else -> Unit
                }
                reportStreamStateChange(streamState)
            }
            .addTo(compositeDisposable)

        createOffer()
    }

    private fun addAudioTrack(factory: PeerConnectionFactory) {
        audioSource = factory.createAudioSource(MediaConstraints())
        audioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource).apply {
            setEnabled(microphoneEnabled)
        }

        val stream = factory.createLocalMediaStream(STREAM_LABEL)
        stream.addTrack(audioTrack)

        peerConnection?.addStream(stream)
    }

    private fun createOffer() {
        peerConnection?.createOffer(
            DefaultSdpObserver(
                onCreateSuccess = { sessionDescription ->
                    sessionDescription?.run { onOfferCreateSuccess(this) }
                },
                onCreateFailure = {
                    Timber.d("sdb observer create failure")
                },
                onSetFailure = {
                    Timber.d("sdb set failure: $it")
                }
            ),
            constraints
        )
    }

    private fun onOfferCreateSuccess(sessionDescription: SessionDescription) {
        peerConnection?.setLocalDescription(DefaultSdpObserver(), sessionDescription)
        rtcClientMessageController.startRtcSession(sessionDescription)
    }

    override fun handleSdpAnswerMessage(sdpData: Message.SdpData) {
        if (isCallConnected) {
            peerConnection?.close()
            isCallConnected = false
        }
        isCallConnected = true
        peerConnection?.setRemoteDescription(
            DefaultSdpObserver(
                onSetSuccess = { Timber.i("Set success") },
                onSetFailure = { message -> Timber.e("Failure: $message") }
            ),
            SessionDescription(SessionDescription.Type.ANSWER, sdpData.sdp)
        )
    }

    override fun handleIceCandidateMessage(iceCandidateData: Message.IceCandidateData) {
        peerConnection?.addIceCandidate(
            IceCandidate(
                iceCandidateData.sdpMid,
                iceCandidateData.sdpMLineIndex,
                iceCandidateData.sdp
            )
        )
    }

    override fun handleBabyDeviceSdpError(error: String) {
        Timber.e(error)
        streamStateListener.invoke(ConnectionState(RtcConnectionState.Error))
    }

    companion object {
        private const val HANDSHAKE_AUDIO_OFFER = "OfferToReceiveAudio"
        private const val HANDSHAKE_VIDEO_OFFER = "OfferToReceiveVideo"
        private const val AUDIO_TRACK_ID = "audio"
        private const val STREAM_LABEL = "stream"

        private const val HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT = "DtlsSrtpKeyAgreement"
    }
}
