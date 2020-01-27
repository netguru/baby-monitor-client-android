package co.netguru.baby.monitor.client.feature.communication.webrtc.server

import android.content.Context
import co.netguru.baby.monitor.client.feature.communication.webrtc.*
import co.netguru.baby.monitor.client.feature.communication.webrtc.observers.ConnectionObserver
import co.netguru.baby.monitor.client.feature.communication.websocket.Message
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.webrtc.*
import timber.log.Timber

class WebRtcManager constructor(
    private val sendMessage: (Message) -> Unit
) {

    private lateinit var peerConnectionFactory: PeerConnectionFactory

    private var cameraVideoCapturer: CameraVideoCapturer? = null
    private lateinit var videoSource: VideoSource
    private lateinit var videoTrack: VideoTrack
    private var audioSource: AudioSource? = null
    private var audioTrack: AudioTrack? = null

    private var peerConnection: PeerConnection? = null
    private var stream: MediaStream? = null
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var connectionObserver: ConnectionObserver

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
            videoCapturer.startCapture(
                VIDEO_WIDTH,
                VIDEO_HEIGHT,
                VIDEO_FRAMERATE
            )
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
        val encoderFactory = DefaultVideoEncoderFactory(sharedContext, true, false)
        val decoderFactory = DefaultVideoDecoderFactory(sharedContext)
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoDecoderFactory(decoderFactory)
            .setVideoEncoderFactory(encoderFactory)
            .createPeerConnectionFactory()

        val surfaceTextureHelper =
            SurfaceTextureHelper.create(SURFACE_TEXTURE_HELPER_THREAD, sharedContext)
        videoSource = peerConnectionFactory.createVideoSource(true)
        cameraVideoCapturer = createCameraCapturer(Camera2Enumerator(context))
            .apply {
                initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
            }

        videoTrack = peerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource)
        cameraVideoCapturer?.let { enableVideo(true, it) }
        createConnection()
    }

    private fun createConnection() {
        connectionObserver = ConnectionObserver()

        peerConnection = peerConnectionFactory.createPeerConnection(
            emptyList(),
            connectionObserver
        )
        listenForIceCandidates(connectionObserver.streamObservable)
    }

    private fun initAudio() {
        audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        audioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource)
    }

    private fun listenForIceCandidates(streamObservable: Observable<StreamState>) {
        streamObservable
            .subscribeOn(Schedulers.io())
            .ofType(OnIceCandidatesChange::class.java)
            .subscribeBy(onNext = { iceCandidateChange ->
                if (iceCandidateChange.iceCandidateState is OnIceCandidateAdded) {
                    handleIceCandidate(
                        iceCandidateChange.iceCandidateState.iceCandidate
                    )
                }
            }, onError = { throwable -> throwable.printStackTrace() })
            .addTo(compositeDisposable)
    }

    private fun handleIceCandidate(iceCandidate: IceCandidate) {
        sendMessage(
            Message(
                iceCandidate = Message.IceCandidateData(
                    iceCandidate.sdp,
                    iceCandidate.sdpMid,
                    iceCandidate.sdpMLineIndex
                )
            )
        )
    }

    fun stopCapturing() {
        Timber.d("stopCapturing()")
        compositeDisposable.dispose()
        audioSource?.dispose()
        videoSource.dispose()
        cameraVideoCapturer?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory.dispose()
    }

    fun acceptOffer(offer: String) {
        Timber.i("acceptOffer($offer)")
        connectionObserver.onAcceptOffer()
        addStream()
        peerConnection?.run {
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
                .subscribeBy(onError = {
                    connectionObserver.onSetDescriptionError()
                    sendMessage(
                        Message(
                            sdpError = it.message
                        )
                    )
                    Timber.e(it)
                })
        }?.addTo(compositeDisposable)
    }

    private fun addStream() {
        Timber.i("Add stream")
        if (stream != null) disposeStream() // If parent enters/exits stream very quick
        initAudio()
        stream = peerConnectionFactory.createLocalMediaStream(STREAM_LABEL)
        stream?.addTrack(audioTrack)
        stream?.addTrack(videoTrack)
        peerConnection?.addStream(stream)
    }

    private fun disposeStream() {
        Timber.i("Dispose stream")
        disposeAudio()
        peerConnection?.removeStream(stream)
        stream = null
    }

    private fun disposeAudio() {
        audioTrack?.dispose()
        audioSource?.dispose()
        audioSource = null
    }

    fun addIceCandidate(iceCandidateData: Message.IceCandidateData) {
        peerConnection?.addIceCandidate(
            IceCandidate(
                iceCandidateData.sdpMid,
                iceCandidateData.sdpMLineIndex,
                iceCandidateData.sdp
            )
        )
    }

    fun addSurfaceView(surfaceViewRenderer: SurfaceViewRenderer) {
        surfaceViewRenderer.init(sharedContext, null)
        videoTrack.addSink(surfaceViewRenderer)
    }

    fun getConnectionObservable(): Observable<RtcConnectionState> =
        connectionObserver.streamObservable.ofType(ConnectionState::class.java)
            .doOnNext {
                if (it.connectionState is RtcConnectionState.Disconnected ||
                    it.connectionState is RtcConnectionState.Error
                ) disposeStream()
            }
            .flatMap { Observable.just(it.connectionState) }

    companion object {
        private const val VIDEO_HEIGHT = 480
        private const val VIDEO_WIDTH = 320
        private const val VIDEO_FRAMERATE = 30
        private const val STREAM_LABEL = "stream"
        private const val AUDIO_TRACK_ID = "audio"
        private const val VIDEO_TRACK_ID = "video"
        private const val SURFACE_TEXTURE_HELPER_THREAD = "SURFACE_TEXTURE_HELPER_THREAD"
    }
}
