package co.netguru.baby.monitor.client.feature.communication.webrtc

import android.content.Context
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.RtcServerConnectionObserver
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.RtcServerConnectionState
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.webrtc.*
import timber.log.Timber

class WebRtcManager constructor(
    private val sendMessage: (Message) -> Unit
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private lateinit var audioSource: AudioSource
    private lateinit var audioTrack: AudioTrack

    private var peerConnection: PeerConnection? = null
    private var peerConnectionDisposable: Disposable? = null
    private lateinit var rtcServerConnectionObserver: RtcServerConnectionObserver

    private val eglBase: EglBase by lazy { EglBase.create() }
    private val sharedContext: EglBase.Context by lazy { eglBase.eglBaseContext }
    var cameraEnabled = true
        set(isEnabled) {
            field = isEnabled
            cameraVideoCapturer?.let { enableVideo(isEnabled, it) }
        }

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

    private fun enableVideo(isEnabled: Boolean, videoCapturer: CameraVideoCapturer) {
        if (isEnabled) {
            Timber.i("enableVideo")
            videoCapturer.startCapture(VIDEO_WIDTH, VIDEO_HEIGHT, VIDEO_FRAMERATE)
        } else {
            Timber.i("disableVideo")
            videoCapturer.stopCapture()
        }
    }

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
        cameraVideoCapturer = createCameraCapturer(Camera2Enumerator(context))
        videoSource = peerConnectionFactory.createVideoSource(cameraVideoCapturer)
        videoTrack = peerConnectionFactory.createVideoTrack("video", videoSource)
        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        audioTrack = peerConnectionFactory.createAudioTrack("audio", audioSource)
        cameraVideoCapturer?.let { enableVideo(true, it) }
        rtcServerConnectionObserver = RtcServerConnectionObserver()

        peerConnection = peerConnectionFactory.createPeerConnection(
            emptyList(),
            rtcServerConnectionObserver
        )

        val stream = peerConnectionFactory.createLocalMediaStream("stream")
        stream.addTrack(audioTrack)
        stream.addTrack(videoTrack)
        peerConnection?.addStream(stream)
    }

    fun stopCapturing() {
        Timber.d("stopCapturing()")
        peerConnectionDisposable?.dispose()
        audioSource.dispose()
        videoSource.dispose()
        cameraVideoCapturer?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory.dispose()
    }

    fun acceptOffer(offer: String) {
        Timber.i("acceptOffer($offer)")
        rtcServerConnectionObserver.onAcceptOffer()
        peerConnectionDisposable = peerConnection?.run {
            setRemoteDescription(SessionDescription(SessionDescription.Type.OFFER, offer))
                .doOnComplete { Timber.d("Offer set as a remote description.") }
                .andThen(createAnswer())
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
                    peerConnection?.setLocalDescription(answer)
                }
                .doOnComplete { Timber.d("Answer set as a local description.") }
                .subscribe()
        }
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
