package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.server.RtcServerConnectionObserver
import co.netguru.baby.monitor.client.feature.server.RtcServerConnectionState
import io.reactivex.Observable
import org.webrtc.*
import timber.log.Timber

class WebRtcManager constructor(
    private val sendMessage: (Message) -> Unit
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private lateinit var videoCapturer: VideoCapturer
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private lateinit var audioSource: AudioSource
    private lateinit var audioTrack: AudioTrack

    private lateinit var peerConnection: PeerConnection
    private lateinit var rtcServerConnectionObserver: RtcServerConnectionObserver

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val sharedContext: EglBase.Context by lazy { eglBase.eglBaseContext }

    private fun createCameraCapturer(cameraEnumerator: CameraEnumerator) =
        cameraEnumerator.deviceNames.asSequence()
            // prefer back-facing cameras
            .sortedBy { deviceName ->
                if (cameraEnumerator.isBackFacing(deviceName)) {
                    1
                } else {
                    2
                }
            }
            .mapNotNull { deviceName ->
                cameraEnumerator.createCapturer(deviceName, null)
            }
            .first()

    fun beginCapturing(context: Context) {
        Timber.d("beginCapturing()")

        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(false)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .createPeerConnectionFactory()
            .apply {
                setVideoHwAccelerationOptions(sharedContext, sharedContext)
            }
        videoCapturer = createCameraCapturer(Camera2Enumerator(context))
        videoSource = peerConnectionFactory.createVideoSource(videoCapturer)
        videoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        audioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
        videoCapturer.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FRAMERATE)
        rtcServerConnectionObserver = RtcServerConnectionObserver()

        peerConnection = peerConnectionFactory.createPeerConnection(
            emptyList(),
            rtcServerConnectionObserver
        )

        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        stream.addTrack(videoTrack)
        peerConnection.addStream(stream)
    }

    fun stopCapturing() {
        Timber.d("stopCapturing()")
        audioSource.dispose()
        videoSource.dispose()
        videoCapturer.dispose()
        peerConnection.dispose()
        peerConnectionFactory.dispose()
    }

    fun acceptOffer(offer: String) {
        Timber.i("acceptOffer($offer)")
        rtcServerConnectionObserver.onAcceptOffer()
        peerConnection
            .setRemoteDescription(SessionDescription(SessionDescription.Type.OFFER, offer))
            .doOnComplete { Timber.d("Offer set as a remote description.") }
            .andThen(peerConnection.createAnswer())
            .doOnSuccess { Timber.d("Answer created.") }
            .flatMapCompletable { answer: SessionDescription ->
                sendMessage(
                    Message(
                        sdpAnswer = Message.SdpData(
                            sdp = answer.description,
                            type = answer.type.canonicalForm()
                        )
                    )
                )
                peerConnection.setLocalDescription(answer)
            }
            .doOnComplete { Timber.d("Answer set as a local description.") }
            .subscribe()
    }

    fun addSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        surfaceViewRenderer.init(sharedContext, null)
        videoTrack.addSink(surfaceViewRenderer)
    }

    fun getConnectionObservable(): Observable<RtcServerConnectionState> =
        rtcServerConnectionObserver.rtcConnectionObservable

    companion object {
        private const val VIDEO_HEIGHT = 480
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_FRAMERATE = 30
    }
}
