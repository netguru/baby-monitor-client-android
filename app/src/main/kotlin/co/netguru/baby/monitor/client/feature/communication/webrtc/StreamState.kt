package co.netguru.baby.monitor.client.feature.communication.webrtc

import org.webrtc.PeerConnection

sealed class StreamState
data class ConnectionState(val connectionState: PeerConnection.IceConnectionState?) : StreamState()
data class GatheringState(val gatheringState: PeerConnection.IceGatheringState?) : StreamState()