package co.netguru.baby.monitor.client.feature.communication.websocket

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("action") private val action: String? = null,
    @SerializedName("value") private val value: String? = null,
    @SerializedName("offerSDP") val sdpOffer: SdpData? = null,
    @SerializedName("answerSDP") val sdpAnswer: SdpData? = null,
    @SerializedName("baby_name") val babyName: String? = null,
    @SerializedName("iceCandidate") val iceCandidate: IceCandidateData? = null,
    @SerializedName("errorSDP") val sdpError: String? = null,
    @SerializedName("pairingCode") val pairingCode: String? = null,
    @SerializedName("pairingResponse") val pairingApproved: Boolean? = null
) {
    fun action() =
        if (action != null && value != null) {
            action to value
        } else {
            null
        }

    data class SdpData(
        @SerializedName("sdp") val sdp: String,
        @SerializedName("type") val type: String
    )

    data class IceCandidateData(
        @SerializedName("candidate") val sdp: String,
        @SerializedName("id") val sdpMid: String,
        @SerializedName("label") val sdpMLineIndex: Int
    )
}
