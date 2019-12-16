package co.netguru.baby.monitor.client.feature.communication.webrtc.client

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import co.netguru.baby.monitor.client.common.view.CustomSurfaceViewRenderer
import co.netguru.baby.monitor.client.feature.communication.webrtc.*
import co.netguru.baby.monitor.client.feature.communication.webrtc.base.RtcMessageHandler
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.ConnectionObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.DefaultSdpObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import com.crashlytics.android.Crashlytics
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import org.webrtc.*
import timber.log.Timber

class RtcClient(
    private val rtcClientMessageController: RtcClientMessageController,
    private val streamStateListener: (streamState: StreamState) -> Unit,
    private val remoteView: CustomSurfaceViewRenderer
) : RtcMessageHandler {

    private val compositeDisposable = CompositeDisposable()
    private val eglBase by lazy { EglBase.create() }
    private var sharedContext: EglBase.Context? = eglBase.eglBaseContext

    private lateinit var constraints: MediaConstraints
    private lateinit var connectionObserver: ConnectionObserver

    private var isCallConnected = false

    private var peerConnection: PeerConnection? = null

    init {
        rtcClientMessageController.rtcMessageHandler = this
    }

    fun startCall(
        context: Context
    ) = Completable.fromAction {
        initRtc(context)
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
                } catch (e: Exception) {
                    Crashlytics.logException(e)
                }
            }
        }
    }

    private fun initRtc(context: Context) {
        Timber.i("initializing")
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions()
        )
        Timber.i("initialized")
        val factory = PeerConnectionFactory.builder()
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
        createConnection(factory ?: return)
    }

    private fun createConnection(factory: PeerConnectionFactory) {
        connectionObserver = ConnectionObserver()

        peerConnection = factory.createPeerConnection(
            emptyList(),
            connectionObserver
        )

        connectionObserver.streamObservable
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
        internal const val WEB_SOCKET_ACTION_KEY = "action"

        private const val HANDSHAKE_AUDIO_OFFER = "OfferToReceiveAudio"
        private const val HANDSHAKE_VIDEO_OFFER = "OfferToReceiveVideo"

        private const val HANDSHAKE_DTLS_SRTP_KEY_AGREEMENT = "DtlsSrtpKeyAgreement"
    }
}
