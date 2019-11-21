package co.netguru.baby.monitor.client.feature.communication.webrtc

import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.PeerConnection

sealed class StreamState
data class ConnectionState(val connectionState: RtcConnectionState) : StreamState()
data class GatheringState(val gatheringState: PeerConnection.IceGatheringState?) : StreamState()
data class OnIceCandidatesChange(val iceCandidateState: IceCandidateState) : StreamState()
data class OnAddStream(val mediaStream: MediaStream) : StreamState()

sealed class IceCandidateState
data class OnIceCandidateAdded(val iceCandidate: IceCandidate) : IceCandidateState()
data class OnIceCandidatesRemoved(val iceCandidates: Array<out IceCandidate>?) : IceCandidateState()

sealed class RtcConnectionState {
    object ConnectionOffer : RtcConnectionState()
    object Completed : RtcConnectionState()
    object Checking : RtcConnectionState()
    object Connected : RtcConnectionState()
    object Disconnected : RtcConnectionState()
    object Error : RtcConnectionState()
}
