package co.netguru.baby.monitor.client.feature.communication.webrtc.base

import co.netguru.baby.monitor.client.feature.communication.websocket.Message

interface RtcMessageHandler {
    fun handleSdpAnswerMessage(sdpData: Message.SdpData)
    fun handleIceCandidateMessage(iceCandidateData: Message.IceCandidateData)
    fun handleBabyDeviceSdpError(error: String)
}
