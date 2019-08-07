package co.netguru.baby.monitor.client.feature.communication.websocket

import com.google.gson.annotations.SerializedName

data class Message(
    private val action: String? = null,
    private val value: String? = null,
    @SerializedName("offerSDP") val sdpOffer: SdpOffer? = null,
    @SerializedName("answerSDP") private val sdpAnswer: SdpAnswer? = null
) {
    fun action() =
        if (action != null && value != null)
            action to value
        else
            null

    data class SdpOffer(
        val sdp: String?,
        val type: String?
    )

    data class SdpAnswer(
        private val sdp: String?,
        private val type: String?
    )
}
