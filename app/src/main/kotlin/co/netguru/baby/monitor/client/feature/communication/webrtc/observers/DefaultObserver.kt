package co.netguru.baby.monitor.client.feature.communication.webrtc.observers

import org.webrtc.*

open class DefaultObserver : PeerConnection.Observer {
    override fun onIceCandidate(iceCandidate: IceCandidate) = Unit
    override fun onDataChannel(dataChannel: DataChannel?) = Unit
    override fun onIceConnectionReceivingChange(receiving: Boolean) = Unit
    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState?) = Unit
    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState?) = Unit
    override fun onAddStream(mediaStream: MediaStream) = Unit
    override fun onSignalingChange(signalingState: PeerConnection.SignalingState?) = Unit
    override fun onIceCandidatesRemoved(iceCandidates: Array<out IceCandidate>?) = Unit
    override fun onRemoveStream(mediaStream: MediaStream?) = Unit
    override fun onRenegotiationNeeded() = Unit
    override fun onAddTrack(rtpReceiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) = Unit
}
