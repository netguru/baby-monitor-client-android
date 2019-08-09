package co.netguru.baby.monitor.client.feature.communication.websocket

import com.google.gson.annotations.SerializedName

data class Message(
    @SerializedName("action") private val action: String? = null,
    @SerializedName("value") private val value: String? = null,
    @SerializedName("offerSDP") val sdpOffer: SdpData? = null,
    @SerializedName("answerSDP") private val sdpAnswer: SdpData? = null,
    @SerializedName("baby_name") val babyName: String? = null
) {
    fun action() =
        if (action != null && value != null)
            action to value
        else
            null

    data class SdpData(
        @SerializedName("sdp") val sdp: String?,
        @SerializedName("type") val type: String?
    )
}
